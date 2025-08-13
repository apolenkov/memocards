package org.apolenkov.application.service;

import java.util.Locale;

import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class UserSettingsService {

  private final UserSettingsRepository repository;

  public UserSettingsService(UserSettingsRepository repository) {
    this.repository = repository;
  }

  public Locale getPreferredLocale(long userId, Locale fallback) {
    return repository.findPreferredLocaleCode(userId).map(Locale::forLanguageTag).orElse(fallback);
  }

  public void setPreferredLocale(long userId, Locale locale) {
    repository.savePreferredLocaleCode(userId, locale.toLanguageTag());
  }
}
