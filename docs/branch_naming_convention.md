# Branch Naming Convention

This document describes the naming convention for Git branches used in the `tiny-broker-api` project.

Following a consistent naming strategy helps keep the codebase clean, readable, and easy to maintain, especially when working with feature branches, CI/CD pipelines, and pull requests.

---

## 📀 General Format

```
<type>/<scope>-<short-description>
```

### Components

- `type`: The purpose of the branch. Examples:
  - `feature`: New feature
  - `bugfix`: Bug fix
  - `hotfix`: Critical fix in production
  - `refactor`: Refactoring without changing functionality
  - `chore`: Maintenance work (e.g., CI, build scripts)
  - `test`: Test-related changes only
- `scope`: The module or context of the change.
  - Examples: `auth`, `order-service`, `monolith`, `infra`, `monitoring`, `portfolio`, etc.
- `short-description`: Brief, kebab-case description of the work.

---

## 📌 Examples

| Purpose                       | Branch Name                            |
| ----------------------------- | -------------------------------------- |
| Add order endpoint            | `feature/monolith-add-order-endpoint`  |
| Fix order fee calculation bug | `bugfix/order-service-fix-fee-calc`    |
| Add GitHub Actions workflow   | `chore/infra-add-github-actions`       |
| Split auth to microservice    | `refactor/monolith-split-auth-service` |
| Update Spring Boot version    | `chore/monolith-bump-spring-version`   |
| Add Prometheus monitoring     | `feature/monitoring-add-prometheus`    |

---

## 🧹 Optional: With Issue ID

If you're using GitHub Issues, Jira, or another tracker, you may include the issue ID for traceability:

```
feature/BRO-123-add-matching-engine
```

---

## ✅ Tips

- Use **lowercase** and **kebab-case** (hyphens instead of spaces or underscores)
- Keep branch names **short but descriptive**
- Prefer scope like `monolith` early in the migration phase; later use actual service names
- Always create branches from `main` or `dev`, depending on your flow

---

> This convention aligns with GitHub Flow and supports future evolution to microservices, infrastructure-as-code (e.g., Terraform, Helm), and CI/CD pipelines.

