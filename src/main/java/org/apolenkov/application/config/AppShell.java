package org.apolenkov.application.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.Theme;
import org.springframework.context.annotation.Configuration;

@Configuration
@Theme(value = "flashcards")
@PageTitle("Memocards")
@Push
public class AppShell implements AppShellConfigurator {}
