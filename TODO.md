# TODO - Memocards Project

**Last updated:** 2025-10-27

---

## 🎯 Planned Tasks

### High Priority (Critical - Blocking Issues)

#### Core Functionality

- [ ] Fix statistics reset issue
- [ ] Fix sorting - updated cards shouldn't move to end (sort by ID/date adding on UI)
- [ ] Fix admin content display and access control
- [ ] Fix delete button activation on character input
- [ ] Fix statistics section expansion on title click

#### Security & Data Integrity

- [ ] Improve deck deletion - confirmation for filled decks with backend validation
- [ ] User existence validation and duplicate card/deck name checks in UI
- [ ] Add validation error messages on user edit form
- [ ] Password reset email functionality with link expiration time

#### Essential Features

- [ ] Implement deck-level source and target language fields
- [ ] Add flashcard-level transcription and example fields
- [ ] Make audio and images optional for flashcards
- [ ] Google API integration with caching for transcription

### Medium Priority (Important - UX Improvements)

#### User Experience

- [ ] Update UI to be more modern with Material Design-inspired calm palette
- [ ] Prioritize world-class attractive UX
- [ ] Card preview on grid row click
- [ ] User self-editing through modal window
- [ ] Statistics redesign for better display
- [ ] Statistics by decks - sort by last game date

#### Game Features

- [ ] Time statistics expansion and display in game
- [ ] Auto-scroll settings in game - transition to next card when time expires

#### Technical Improvements

- [ ] Remove all inline styles
- [ ] Break down views into parts and extract logic to use cases
- [ ] Migrate to environment variables
- [ ] Audit logging coverage
- [ ] Add @Slf4j for logging

### Low Priority (Nice to Have - Technical Debt)

#### Code Quality & Refactoring

- [ ] Remove version field from deck_daily_stats
- [ ] Audit CSS classes existence in views
- [ ] General database structure review
- [ ] Limit users to single role
- [ ] Remove isKnow field - unnecessary
- [ ] Remove deleteExpiredTokens method
- [ ] Rename flashcard to memo
- [ ] Remove auto commit
- [ ] Optimize Lombok usage in base and adjust checkstyle
- [ ] Remove redundant tests

#### Additional Features

- [ ] Additional test coverage
- [ ] API with Swagger UI
- [ ] Full-text search implementation
- [ ] Database settings (language switcher)
- [ ] Create changelog

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

## 🐛 Known Issues

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
