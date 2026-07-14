# Trading application

## Gradle setup

1. Open the project in IntelliJ IDEA.
2. Right-click `build.gradle.kts`.
3. Select **Link Gradle Project**.
4. Wait until Gradle synchronization finishes.
5. Open the terminal in the project root.


Build the application:
```powershell
.\gradlew.bat clean build
```

Run the application:

```powershell
.\gradlew.bat run
```

## Frontend setup

Download Node.js with winget (if you don't have winget installed then you should install it)
```powershell
winget install --id OpenJS.NodeJS.LTS -e
```
restart terminal

- from root
```powershell
cd trading-frontend
npm install
```
run localhost server
```powershell
npm run dev
```

