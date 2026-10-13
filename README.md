# Employee Management System

```
employee-management-system/
├── backend/    Spring Boot 3 (Java 17) REST API + MySQL
└── frontend/   Node.js (Express) server + HTML/CSS/JS UI
```

**Admin login:** username `amulya` / password `123456789` (created automatically on first start, stored BCrypt-hashed).

## 1. Run the backend (port 8080)
1. Install Java 17+, Maven, MySQL and start MySQL.
2. Edit `backend/src/main/resources/application.properties` → set your MySQL `username` / `password`.
   The database `ems_db` and all tables are created automatically.
3. ```
   cd backend
   mvn spring-boot:run
   ```

## 2. Run the frontend (port 3000)
```
cd frontend
npm install
npm start
```
Open http://localhost:3000 and login. To point to a different API: `API_URL=http://host:8080/api npm start`

## REST API (all need `Authorization: Bearer <token>` except login)
| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/auth/login | Admin login → token |
| POST | /api/auth/logout | Logout |
| GET | /api/dashboard | Totals & payroll stats |
| GET/POST | /api/employees | List / add employee |
| PUT | /api/employees/{id} | Edit details |
| PUT | /api/employees/{id}/assign | Assign department / designation |
| PUT | /api/employees/{id}/salary | Set base salary |
| DELETE | /api/employees/{id} | Remove employee |
| GET/POST | /api/departments | List / add department |
| DELETE | /api/departments/{id} | Delete (if empty) |
| POST | /api/salaries/pay | Pay salary `{employeeId, month, bonus, deductions}` |
| GET | /api/salaries | All payments |
| GET | /api/salaries/employee/{id} | Payments of one employee |
