# PLACSP Monitor

Buscador web de licitaciones publicadas en la Plataforma de Contratación del Sector Público (PLACSP).

Consume el feed ATOM de sindicación, almacena las licitaciones en base de datos y ofrece una interfaz web con filtros dinámicos.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4, Spring Data JPA, H2/PostgreSQL
- **Frontend:** React 18, Vite

## Inicio rápido

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
Disponible en http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Disponible en http://localhost:5173

## Filtros por defecto

- Tipo de contrato: Servicios
- Estado: En plazo (PUB)
- CPV: 72* (servicios IT)
