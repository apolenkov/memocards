package org.apolenkov.application.config;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.vaadin.flow.i18n.I18NProvider;

@Component
public class AppI18NProvider implements I18NProvider {

  public static final String BUNDLE_PREFIX = "i18n.messages";
  private static final List<Locale> PROVIDED_LOCALES =
      Arrays.asList(Locale.ENGLISH, new Locale("ru"), new Locale("es"));

  @Override
  public List<Locale> getProvidedLocales() {
    return PROVIDED_LOCALES;
  }

  @Override
  public String getTranslation(String key, Locale locale, Object... params) {
    if (key == null) {
      return "";
    }
    final Locale used = locale != null ? locale : Locale.ENGLISH;
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PREFIX, used);
      String value = bundle.containsKey(key) ? bundle.getString(key) : key;
      return params != null && params.length > 0 ? MessageFormat.format(value, params) : value;
    } catch (MissingResourceException e) {
      return key;
    }
  }
}
