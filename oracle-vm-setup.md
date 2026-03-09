# Oracle Cloud VM Setup — Maple Blog

> **Shape: VM.Standard.A1.Flex (Always Free)** — ARM64 (Ampere)
> Allocate 2 OCPUs / 12 GB RAM (up to 4 OCPU / 24 GB, all free).

---

## 1. Connect

```bash
ssh -i ~/.ssh/{YOUR_KEY}.key opc@{YOUR_VM_PUBLIC_IP}
```

---

## 2. System Update

```bash
sudo dnf update -y
```

> Run `sudo dnf update -y` periodically to keep nginx, PostgreSQL, Java, and OS security patches current. Restart services after: `sudo systemctl restart nginx postgresql maple`

---

## 3. Java 21

```bash
sudo dnf install -y java-21-openjdk
java -version
```

---

## 4. PostgreSQL

```bash
sudo dnf install -y postgresql-server postgresql
sudo postgresql-setup --initdb
sudo systemctl enable --now postgresql
```

```bash
sudo -u postgres psql
```

```sql
CREATE DATABASE mapledb;
CREATE USER mapleuser WITH PASSWORD '{DB_PASSWORD}';
GRANT ALL PRIVILEGES ON DATABASE mapledb TO mapleuser;
\q
```

### Switch to password authentication

By default PostgreSQL uses ident/peer (OS username check) — the app needs md5 (password-based):

```bash
sudo nano /var/lib/pgsql/data/pg_hba.conf
```

Change only these three lines (leave replication lines alone):
```
local   all   all                       peer   → md5
host    all   all   127.0.0.1/32        ident  → md5
host    all   all   ::1/128             ident  → md5
```

```bash
sudo systemctl restart postgresql
```

---

## 5. Nginx

```bash
sudo dnf install -y nginx
sudo systemctl enable --now nginx
```

### nginx.conf

```bash
sudo nano /etc/nginx/nginx.conf
```

Add inside `http { ... }` before the `include` lines:
```nginx
limit_req_zone $binary_remote_addr zone=maple:10m rate=20r/s;
server_tokens off;
```

Delete the default `server { listen 80 ... }` block entirely — it conflicts with `maple.conf`.

### Site config

```bash
sudo nano /etc/nginx/conf.d/maple.conf
```

```nginx
# Only write this block — certbot will automatically append the HTTP→HTTPS
# redirect block and SSL certificate paths when you run it in step 11.
server {
    listen 80;
    server_name {YOUR_DOMAIN} www.{YOUR_DOMAIN};

    limit_req zone=maple burst=50 nodelay;
    client_max_body_size 55M;

    location /uploads/ {
        alias /opt/maple/uploads/;
        expires 30d;
        add_header Cache-Control "public";
    }

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

> After running certbot (step 11), it rewrites this file to add a `listen 443 ssl` block with certificate paths and a separate `listen 80` redirect block. Do not manually edit the SSL sections — certbot manages them.

```bash
sudo nginx -t && sudo systemctl reload nginx
```

### SELinux — allow Nginx to proxy

```bash
sudo setsebool -P httpd_can_network_connect 1
```

### Firewall

```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo systemctl disable --now rpcbind rpcbind.socket
sudo firewall-cmd --reload
```

In **OCI Console → Networking → VCN → Security Lists** add ingress rules:
- TCP 80 from 0.0.0.0/0
- TCP 443 from 0.0.0.0/0

> Do NOT open port 8080.

---

## 6. Bandwidth Guard (Optional but recommended if cost conscious)

Blocks outbound traffic (ports 80/443) if daily outbound exceeds 50 GB. Runs every 5 minutes via cron.

Should not activate unless there is very high natural traffic or a DDOS, good for stopping the network and monitoring before any damages.

> Confirm your interface name: `ip link show` — typically `enp0s3` on E2 Micro, `enp0s6` on A1 Flex.

```bash
sudo mkdir -p /var/lib/bandwidth-check
sudo nano /usr/local/bin/bandwidth-check.sh
```

```bash
#!/bin/bash
LIMIT_BYTES=53687091200   # 50GB
IFACE={YOUR_INTERFACE}         # confirm with: ip link show
PERSIST_DIR=/var/lib/bandwidth-check
DAY_START_FILE=$PERSIST_DIR/day_start_tx
DATE_FILE=$PERSIST_DIR/last_date
LOG=/var/log/bandwidth-check.log
TODAY=$(date +%Y-%m-%d)

mkdir -p "$PERSIST_DIR"

CURRENT_TX=$(cat /sys/class/net/$IFACE/statistics/tx_bytes)

if [ "$(cat $DATE_FILE 2>/dev/null)" != "$TODAY" ]; then
    echo "$TODAY" > "$DATE_FILE"
    echo "$CURRENT_TX" > "$DAY_START_FILE"
    echo "$(date): New day — baseline set to $CURRENT_TX bytes." >> "$LOG"
fi

DAY_START=$(cat "$DAY_START_FILE" 2>/dev/null)
[ -z "$DAY_START" ] && DAY_START=$CURRENT_TX

if [ "$CURRENT_TX" -lt "$DAY_START" ]; then
    echo "$CURRENT_TX" > "$DAY_START_FILE"
    DAY_START=$CURRENT_TX
    echo "$(date): Reboot detected — baseline reset." >> "$LOG"
fi

USED_TODAY=$((CURRENT_TX - DAY_START))
USED_GB=$(echo "scale=2; $USED_TODAY / 1073741824" | bc)
echo "$(date): Outbound today = ${USED_GB}GB / 50GB limit" >> "$LOG"

if [ "$USED_TODAY" -gt "$LIMIT_BYTES" ]; then
    echo "$(date): LIMIT EXCEEDED — blocking ports 80/443." >> "$LOG"
    systemctl stop nginx
    firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="0.0.0.0/0" port port="80" protocol="tcp" drop'
    firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="0.0.0.0/0" port port="443" protocol="tcp" drop'
    firewall-cmd --reload
    echo "$(date): Done. SSH still accessible." >> "$LOG"
fi
```

```bash
sudo chmod +x /usr/local/bin/bandwidth-check.sh
sudo crontab -e
```

Add:
```
*/5 * * * * /usr/local/bin/bandwidth-check.sh
```

### Test

```bash
sudo sed -i 's/LIMIT_BYTES=53687091200/LIMIT_BYTES=1/' /usr/local/bin/bandwidth-check.sh
sudo /usr/local/bin/bandwidth-check.sh
tail -20 /var/log/bandwidth-check.log
sudo systemctl status nginx
sudo firewall-cmd --list-rich-rules

# Restore after test:
sudo sed -i 's/LIMIT_BYTES=1$/LIMIT_BYTES=53687091200/' /usr/local/bin/bandwidth-check.sh
sudo rm /var/lib/bandwidth-check/*
sudo systemctl start nginx
sudo firewall-cmd --permanent --remove-rich-rule='rule family="ipv4" source address="0.0.0.0/0" port port="80" protocol="tcp" drop'
sudo firewall-cmd --permanent --remove-rich-rule='rule family="ipv4" source address="0.0.0.0/0" port port="443" protocol="tcp" drop'
sudo firewall-cmd --reload
```

### Re-enable after a real block

```bash
sudo systemctl start nginx
sudo firewall-cmd --permanent --remove-rich-rule='rule family="ipv4" source address="0.0.0.0/0" port port="80" protocol="tcp" drop'
sudo firewall-cmd --permanent --remove-rich-rule='rule family="ipv4" source address="0.0.0.0/0" port port="443" protocol="tcp" drop'
sudo firewall-cmd --reload
```

---

## 7. OCI Budget Alert

**Billing & Cost Management → Budgets → Create Budget**
- Amount: `$1`, threshold: 100%, add your email.

---

## 8. App Directory & Jar

```bash
sudo mkdir -p /opt/maple/uploads
sudo chown opc:opc /opt/maple /opt/maple/uploads
```

From local machine:
```bash
./gradlew bootJar
scp -i ~/.ssh/{YOUR_KEY}.key build/libs/maple-*.jar opc@{YOUR_VM_PUBLIC_IP}:/opt/maple/maple.jar
```

---

## 9. Environment File

```bash
sudo nano /opt/maple/.env
```

```
SPRING_PROFILES_ACTIVE=prod
DB_NAME=mapledb
DB_USERNAME=mapleuser
DB_PASSWORD={DB_PASSWORD}
UPLOAD_DIR=/opt/maple/uploads
SEED_ADMIN_EMAIL={ADMIN_EMAIL}
SEED_ADMIN_PASSWORD={ADMIN_PASSWORD}
SEED_ADMIN_FIRST_NAME={ADMIN_FIRST_NAME}
SEED_ADMIN_LAST_NAME={ADMIN_LAST_NAME}
```

```bash
sudo chown root:root /opt/maple/.env
sudo chmod 600 /opt/maple/.env
```

---

## 10. Systemd Service

```bash
sudo nano /etc/systemd/system/maple.service
```

```ini
[Unit]
Description=Maple Blog
After=network.target postgresql.service
Requires=postgresql.service

[Service]
User=opc
WorkingDirectory=/opt/maple
EnvironmentFile=/opt/maple/.env
ExecStart=java -Xmx2g -Xms512m -jar /opt/maple/maple.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now maple
sudo systemctl status maple
```

---

## 11. HTTPS

### DNS — add at your registrar:
```
A  @    {YOUR_VM_PUBLIC_IP}  TTL 300
A  www  {YOUR_VM_PUBLIC_IP}  TTL 300
```

Verify propagation from your local machine:
```bash
dig {YOUR_DOMAIN} +short
```

### Issue certificate

```bash
sudo dnf install -y python3-pip augeas-libs python3-augeas
sudo pip3 install certbot certbot-nginx

# pip doesn't create the certbot binary on Oracle Linux 9 ARM — run via python directly:
sudo python3 -c "
import sys
sys.argv = ['certbot', '--nginx', '-d', '{YOUR_DOMAIN}', '-d', 'www.{YOUR_DOMAIN}']
from certbot.main import main
main()
"
sudo systemctl reload nginx
```

### Auto-renewal cron

```bash
sudo crontab -e
```

Add alongside the bandwidth guard line:
```
0 3 * * * python3 -c "import sys; sys.argv = ['certbot', 'renew', '--quiet']; from certbot.main import main; main()"
```

Test renewal:
```bash
sudo python3 -c "
import sys
sys.argv = ['certbot', 'renew', '--dry-run']
from certbot.main import main
main()
"
```

### Changing domain later:
1. Point new domain A records to VM IP
2. Update `server_name` in `/etc/nginx/conf.d/maple.conf`
3. Re-run certbot with the new domain
4. Old cert expires naturally — no action needed

---

## 12. Updating the App

```bash
# Local
./gradlew bootJar
scp -i ~/.ssh/{YOUR_KEY}.key build/libs/maple-*.jar opc@{YOUR_VM_PUBLIC_IP}:/opt/maple/maple.jar

# VM
sudo systemctl restart maple
sudo journalctl -u maple -f
```


---

## 13. Database Backup & Restore

```bash
# Dump to file
sudo -u postgres pg_dump mapledb > ~/mapledb-backup-$(date +%Y%m%d).sql

# Copy to local machine
scp -i ~/.ssh/{YOUR_KEY}.key opc@{YOUR_VM_PUBLIC_IP}:~/mapledb-backup-*.sql .

# Restore from file
sudo -u postgres psql mapledb < mapledb-backup-YYYYMMDD.sql
```

---

## Monitoring & Health Commands

### Services
```bash
sudo systemctl status maple nginx postgresql   # status of all three at once
sudo journalctl -u maple -n 50                 # last 50 app log lines
sudo journalctl -u maple -f                    # follow app logs live
sudo journalctl -u nginx -n 20                 # recent nginx logs
sudo tail -50 /var/log/nginx/access.log        # incoming requests
sudo tail -50 /var/log/nginx/error.log         # nginx errors
```

### Memory & CPU
```bash
free -h                                        # RAM usage summary
top                                            # live processes
ps aux --sort=-%mem | head -10                 # top 10 memory consumers
```

### Disk
```bash
df -h                                          # disk usage
du -sh /opt/maple/uploads                      # uploads folder size
```

### Network & Bandwidth
```bash
tail -20 /var/log/bandwidth-check.log          # today's outbound usage
sudo ss -tlnp                                  # all listening ports
sudo firewall-cmd --list-all                   # active firewall rules
cat /sys/class/net/{YOUR_INTERFACE}/statistics/tx_bytes  # raw outbound byte counter
cat /sys/class/net/{YOUR_INTERFACE}/statistics/rx_bytes  # raw inbound byte counter
```

### PostgreSQL Health
```bash
# Connect to the database
sudo -u postgres psql mapledb

# Useful queries inside psql:
\dt                                            -- list all tables
\di                                            -- list all indexes
SELECT count(*) FROM account;                  -- number of accounts
SELECT count(*) FROM post;                     -- number of posts
SELECT pg_size_pretty(pg_database_size('mapledb'));  -- total database size
SELECT pg_size_pretty(pg_total_relation_size('post'));  -- post table size

# Check active connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'mapledb';

# Check for long-running queries
SELECT pid, now() - query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active' AND now() - query_start > interval '5 seconds';

\q  -- exit psql
```

### SSL Certificate
```bash
sudo python3 -c "
import sys
sys.argv = ['certbot', 'certificates']
from certbot.main import main
main()
"
```
