# NexusFi - Run Scripts Guide

This folder contains convenient scripts to run the application easily.

---

## 🚀 Quick Start

### Windows Command Prompt (.bat files):

```cmd
# Run the application in development mode
scripts\run-dev.bat

# Build the project
scripts\build.bat

# Package as JAR
scripts\package.bat
```

### Windows PowerShell (.ps1 files):

```powershell
# Run the application in development mode
.\scripts\run-dev.ps1
```

---

## 📝 Script Details

### `run-dev.bat` / `run-dev.ps1`

**Purpose:** Run the application in development mode  
**What it does:**

- Sets database password as environment variable
- Starts Spring Boot with `dev` profile
- Connects to PostgreSQL database
- Application runs on http://localhost:8080

**When to use:** Daily development work

---

### `build.bat`

**Purpose:** Clean and compile the project  
**What it does:**

- Deletes `target/` folder
- Compiles all Java files
- Downloads dependencies if needed

**When to use:**

- After pulling code from Git
- When you want a fresh build
- After changing dependencies in `pom.xml`

---

### `package.bat`

**Purpose:** Create a runnable JAR file  
**What it does:**

- Clean build
- Compiles and packages application
- Creates `nexusfi-1.0.0-SNAPSHOT.jar` in `target/` folder
- Skips tests for faster build

**When to use:**

- Before deploying to production
- When you want to run the app without Maven

---

## 🔧 Configuration

### Change Database Password

Edit `run-dev.bat` or `run-dev.ps1` and change this line:

**In .bat:**

```cmd
set DB_PASSWORD=your_new_password
```

**In .ps1:**

```powershell
$env:DB_PASSWORD="your_new_password"
```

---

## 🎯 Without Scripts (Manual Commands)

If you prefer typing commands:

### Run in dev mode:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev -Dspring-boot.run.jvmArguments=-DDB_PASSWORD=postgres
```

### Build:

```bash
mvn clean compile
```

### Package:

```bash
mvn clean package
```

---

## ⚠️ Important Notes

1. **Scripts are in `.gitignore`** - They contain your database password and won't be committed to Git
2. **Maven must be in PATH** - If you get "mvn not found", restart your terminal
3. **Application runs until you stop it** - Press `Ctrl+C` to stop
4. **Port 8080 must be free** - Application won't start if another app uses port 8080

---

## 🐛 Troubleshooting

### "mvn is not recognized"

**Solution:** Close and reopen your terminal (Maven PATH was just updated)

### "Failed to connect to database"

**Solution:**

1. Make sure PostgreSQL is running
2. Check password in the script matches your database password
3. Verify database `nexusfi` exists

### Application won't start

**Solution:**

1. Run `build.bat` first
2. Check if port 8080 is in use: `netstat -ano | findstr :8080`
3. Check terminal output for error messages

---

## 📚 Next Steps

After running `run-dev.bat`:

1. Open browser: http://localhost:8080
2. You'll see a login page (Spring Security)
3. Default credentials are in `application.yml`

---

**Happy coding!** 🚀
