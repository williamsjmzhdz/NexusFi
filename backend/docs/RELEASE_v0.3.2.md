# NexusFi v0.3.2 - Backend Repository Reorganization

**Release Date:** March 15, 2026  
**Status:** Production Ready
**Live URL:** https://nexusfi-production.up.railway.app/api/v1

---

## What's New

This release focuses on a professional backend repository cleanup and structure standardization.

---

## Backend Structure Cleanup

- Backend project consolidated under `backend/`
- Source code, configuration, scripts, docs, and database assets organized by domain
- Development helper scripts moved to `backend/scripts/`
- Updated script references in docs to the new paths

---

## Git Hygiene Improvements

- Added backend-focused ignore rules in `backend/.gitignore`
- Added repo root `.gitignore` entry for IDE metadata (`.idea/`)
- Removed generated/local artifacts from tracked project layout

---

## CI/CD and Deployment Notes

- `main` and `develop` were homologated to the same commit
- Health endpoint remains available at `/api/v1/auth/health`
- Railway should deploy from `main` with Docker context rooted to backend settings

---

## Verification Snapshot

- Branch alignment confirmed (`main` = `develop`)
- Remote alignment confirmed (`origin/main` = `origin/develop`)
- Production health check: `200 OK` with body `OK`

