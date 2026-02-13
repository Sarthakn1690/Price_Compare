# How to run in VS Code / Cursor

## Important: open the right folder

- **Open this folder in VS Code:** `c:\Users\user\SEPM`  
  (the folder that contains **backend** and **frontend**)
- **File → Open Folder** → select **SEPM** (not a subfolder).

---

## Method A: Run Task

1. Press **Ctrl+Shift+P** (or F1).
2. Type **Run Task** and choose **Tasks: Run Task**.
3. Pick **Start Backend** or **Start Frontend**.
4. **Start Backend** first. Wait until you see: `Started PriceComparisonApplication`.
5. Run **Start Frontend** in a second terminal (Run Task again → **Start Frontend**).
6. In the browser open: **http://localhost:5173**

---

## Method B: Run the .bat files

1. In the **Explorer** (left sidebar), go to the **SEPM** folder (root).
2. **Right‑click** `start-backend.bat` → **Open in Integrated Terminal**  
   (or double‑click in Explorer if your OS runs it).
3. Wait until the backend has started.
4. **Right‑click** `start-frontend.bat` → **Open in Integrated Terminal**.
5. Open **http://localhost:5173** in your browser.

---

## Method C: Manual in Terminal

1. **Terminal → New Terminal** (or Ctrl+`).
2. Backend:
   ```bat
   cd backend
   mvnw.cmd spring-boot:run
   ```
3. **Terminal → New Terminal** (click **+**).
4. Frontend:
   ```bat
   cd frontend
   npm run dev
   ```
5. Open **http://localhost:5173**.

---

## If "Run Task" shows no tasks

- Confirm the **folder opened** in VS Code is **SEPM** (you should see **backend** and **frontend** in the Explorer).
- Reload the window: **Ctrl+Shift+P** → **Developer: Reload Window**.

## If port 8080 is already in use

In a terminal run:

```bat
netstat -ano | findstr :8080
taskkill /PID <number_from_last_column> /F
```

Then start the backend again.
