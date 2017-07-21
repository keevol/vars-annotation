package org.mbari.m3.vars.annotation.events;

import com.google.common.collect.ImmutableList;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.Collection;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:18:00
 */
public class AnnotationsChangedEvent extends UIChangeEvent<Collection<Annotation>> {

    public AnnotationsChangedEvent(Object changeSource, Collection<Annotation> annotations) {
        super(changeSource, ImmutableList.copyOf(annotations));
    }
}
