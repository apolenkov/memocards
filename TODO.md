# TODO - Memocards Project

**Last updated:** 2025-10-27

---

## 🎯 Planned Tasks

### High Priority

- [ ] Implement deck-level source and target language fields
- [ ] Add flashcard-level transcription and example fields
- [ ] Make audio and images optional for flashcards
- [ ] Update UI to be more modern with Material Design-inspired calm palette
- [ ] Prioritize world-class attractive UX

### Medium Priority

- [ ] 

### Low Priority

- [ ] 

---

## 📋 Future Ideas

### Infrastructure

- [ ] Molecule tests for Ansible roles
- [ ] Staging environment (separate docker-compose)
- [ ] Renovate bot for postgres image updates
- [ ] CodeQL scanning for Java code
- [ ] fail2ban on VPS for additional security

### Monitoring & Observability

- [ ] Grafana dashboard for deployment metrics
- [ ] Prometheus exporter for deployment history
- [ ] Slack/Telegram webhook notifications after deployment
- [ ] Alerting for long periods without deployments (>1 week)
- [ ] Web UI for viewing deployment history

### Deployment

- [ ] Rollback with deployment selection from history
- [ ] Blue-green deployment strategy
- [ ] Canary deployments
- [ ] Log failed deployments in rescue block

### Development

- [ ] Pre-commit hooks for Ansible lint
- [ ] Local development with docker-compose
- [ ] Integration tests for deployment process

### Documentation

- [ ] Architecture Decision Records (ADR)
- [ ] Runbook for typical issues
- [ ] Onboarding guide for new developers

---

## 🚫 Won't Do

- ~~Multi-region deployment~~ (not needed for MVP)
- ~~Kubernetes migration~~ (overkill for current scale)
- ~~Microservices architecture~~ (monolith works great)

---

## 🐛 Known Issues

### Minor

- [ ] IDE warnings for Ansible Jinja2 expressions (doesn't affect functionality)

### Won't Fix

- Dependabot doesn't monitor Docker images (we use Jib, no Dockerfile)

---

## 💡 Notes

### Priority Levels

- **High**: Critical for production, blocking issues, security vulnerabilities
- **Medium**: Improves UX/DX, optimizations, nice-to-have features
- **Low**: Ideas, experiments, long-term improvements

### Workflow

1. Add new tasks to "Planned Tasks" section
2. Use checkboxes `- [ ]` for tracking
3. Mark as complete with [x] and remove from list
4. Keep file focused on active work only

### Categories

- **Infrastructure**: CI/CD, Ansible, Docker, deployments
- **Monitoring**: Logs, metrics, alerts, observability
- **Development**: Code quality, tests, tools
- **Documentation**: Docs, guides, ADRs
- **Security**: Vulnerabilities, compliance, hardening
- **Performance**: Optimization, caching, speed

---

## 📊 Statistics

- **Active tasks:** 5 (High priority)
- **Backlog ideas:** 15+

---

## 🔗 Links

- [GitHub Actions Workflows](.github/workflows/)
- [Ansible Playbooks](ansible/playbooks/)
- [Deployment History Guide](ansible/DEPLOYMENT_HISTORY.md)
- [Project Documentation](docs/) (to be created)

---

**How to use:**

```bash
# Add a new task
echo "- [ ] Your task here" >> TODO.md

# Mark task complete (manually in editor)
# Change [ ] to [x], then remove from file

# View active tasks
grep "^- \[ \]" TODO.md

# View all planned
cat TODO.md
```

---

**Start adding your tasks! ✍️**
