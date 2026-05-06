# Implementation Plan: MVP Seeding and Demo Script

## Phase 1: Database Seed Script
- [ ] Task: Create a `docker/init-data.sql` script with `INSERT` statements for basic setup (Igreja, Salão, Catequese, Liturgia, and default users/memberships). Ensure `calendario.organizacoes` table is created here since it's an external dependency.
- [ ] Task: Update `docker-compose.yml` to mount `docker/init-data.sql` to `/docker-entrypoint-initdb.d/` in the PostgreSQL container.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Database Seed Script' (Protocol in workflow.md)

## Phase 2: Demo Script
- [ ] Task: Create `demo_mvp.sh` in the project root. This script will use `curl` to interact with `http://localhost:8080`.
- [ ] Task: Implement the flows in `demo_mvp.sh`: Create Project, Create standard Events, Update Event (triggering approval), Execute Approval, and Fetch Project Aggregation. Extract IDs from JSON responses to chain the requests.
- [ ] Task: Ensure `demo_mvp.sh` is executable (`chmod +x`).
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Demo Script' (Protocol in workflow.md)