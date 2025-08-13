package org.apolenkov.application.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("stats")
@AnonymousAllowed
public class StatsView extends VerticalLayout implements HasDynamicTitle {
  public StatsView() {
    setWidth("100%");
    add(new H2(getTranslation("stats.page.underConstruction")));
  }

  @Override
  public String getPageTitle() {
    return getTranslation("stats.page.title");
  }
}
