
# Second-hand-EV-Battery-Trading-Platform

Here is how to run the project, call api




## System Requirements

- **Java 21+**
- **Maven 3.8+**
- **SQL Server 20**
- **IDE: IntelliJ IDEA**
## Step to run project

### Step 1: Install IntelliJ's latest version

Link: https://www.jetbrains.com/idea/download/?section=windows

If you're still a college student, you can request the PRO version, followed by this link: https://www.jetbrains.com/academy/student-pack/


### Step 2: Clone project

Pre-requisite: IntelliJ is available on your local computer

1. Open windows explorer
2. Choose the directory to clone 
3. Open git bash and copy paste this:

```bash
git clone https://github.com/gAmma05/Second-hand-EV-Battery-Trading-Platform

```

### Step 3: Open the repository folder through IDE

1. Open IntelliJ IDE
2. Open the folder where repository is placed

### Step 4: Environment variables setup

1. Create a file, names it ".env", put it into the folder where application.properties is placed

2. Copy paste this into .env:
```env
MAIL_USERNAME=
MAIL_PASSWORD=

SERVER_PORT=

DB_URL=
DB_USERNAME=sa
DB_PASSWORD=12345

REDIS_HOST=localhost
REDIS_PORT=6370
REDIS_PASSWORD=

JWT_SECRET=
JWT_ISSUER=

GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

To fill in these fields, check out these website to generate:

**GOOGLE APP PASSWORD**: https://support.google.com/accounts/answer/185833?hl=en

**GOOGLE CLIENT ID**: https://console.cloud.google.com/

**JWT GENERATOR**: https://jwtsecrets.com/

### Step 5: Install Redis ###

- You can consider 2 ways: Install directly Redis or install it through Docker (***Docker Desktop*** is required)

Redis for Windows: https://github.com/tporadowski/redis/releases

Docker Desktop: https://www.docker.com/products/docker-desktop/

**NOTES**: If you choose the Docker Desktop, after getting it installed, run it through Start Menu or Desktop

Get in the project's terminal (open it in IDE), copy paste this:

```terminal
docker run --name redis-server -p 6379:6379 -d redis
```

Check it if it's running or not

```terminal
docker ps
```

If the terminal shows like this (it could be a little different from this below):
```terminal
CONTAINER ID   IMAGE   COMMAND   PORTS           NAMES
abc123         redis   "redis-server"   0.0.0.0:6379->6379/tcp   redis-server
```
Then it's working

