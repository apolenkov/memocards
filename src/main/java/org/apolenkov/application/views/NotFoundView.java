package org.apolenkov.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.server.HttpStatusCode;

@ParentLayout(PublicLayout.class)
public class NotFoundView extends Div implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        event.rerouteTo("");
        return HttpStatusCode.NOT_FOUND.getCode();
    }
}
