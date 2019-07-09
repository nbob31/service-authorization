/*
 * Copyright 2019 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.auth.integration.saml;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml.SamlAuthentication;
import org.springframework.security.saml.saml2.authentication.Assertion;
import org.springframework.security.saml.saml2.authentication.SubjectPrincipal;
import org.springframework.security.saml.spi.DefaultSamlAuthentication;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Information extracted from SAML response
 *
 * @author Yevgeniy Svalukhin
 */
public class ReportPortalSamlAuthentication implements SamlAuthentication {

    private static final long serialVersionUID = -289812989450932L;

    private boolean authenticated;
    private Subject subject;
    private List<Attribute> attributes = new LinkedList<>();
    private List<? extends GrantedAuthority> grantedAuthorities;
    private transient Assertion assertion;
    private String assertingEntityId;
    private String holdingEntityId;
    private String relayState;
    private String responseXml;
    private String issuer;

    public ReportPortalSamlAuthentication(boolean authenticated,
                                          Assertion assertion,
                                          String assertingEntityId,
                                          String holdingEntityId,
                                          String relayState) {
        this.authenticated = authenticated;
        this.assertingEntityId = assertingEntityId;
        this.holdingEntityId = holdingEntityId;
        this.relayState = relayState;
        this.assertion = assertion;
        fillSubject(assertion);
        fillAttributes(assertion);
        issuer = assertion.getIssuer().getValue();
    }

    public ReportPortalSamlAuthentication(DefaultSamlAuthentication defaultSamlAuthentication) {
        this(defaultSamlAuthentication.isAuthenticated(),
                defaultSamlAuthentication.getAssertion(),
                defaultSamlAuthentication.getAssertingEntityId(),
                defaultSamlAuthentication.getHoldingEntityId(),
                defaultSamlAuthentication.getRelayState());
    }

    private void fillAttributes(Assertion assertion) {
        List<Attribute> mappedAttributes = assertion.getAttributes().stream()
                .map(attr -> new Attribute()
                        .setName(attr.getName())
                        .setFriendlyName(attr.getFriendlyName())
                        .setNameFormat(attr.getNameFormat().toString())
                        .setRequired(attr.isRequired())
                        .setValues(attr.getValues()))
                .collect(Collectors.toList());
        attributes.addAll(mappedAttributes);
    }

    private void fillSubject(Assertion assertion) {
        subject = new Subject()
                .setPrincipal(new Principal()
                        .setFormat(assertion.getSubject().getPrincipal().getFormat().getFormat().toString())
                        .setValue(assertion.getSubject().getPrincipal().getValue()));
    }

    @Override
    public String getAssertingEntityId() {
        return assertingEntityId;
    }

    @Override
    public String getHoldingEntityId() {
        return holdingEntityId;
    }

    @Override
    public SubjectPrincipal<? extends SubjectPrincipal> getSamlPrincipal() {
        return subject.getPrincipal();
    }

    @Override
    public Assertion getAssertion() {
        return assertion;
    }

    @Override
    public String getRelayState() {
        return relayState;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }

    protected void setHoldingEntityId(String holdingEntityId) {
        this.holdingEntityId = holdingEntityId;
    }

    protected void setAssertingEntityId(String assertingEntityId) {
        this.assertingEntityId = assertingEntityId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public Subject getCredentials() {
        return subject;
    }

    @Override
    public List<Attribute> getDetails() {
        return attributes;
    }

    @Override
    public String getPrincipal() {
        return subject.getPrincipal().getValue();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (!authenticated && isAuthenticated) {
            throw new IllegalArgumentException("Unable to change state of an existing authentication object.");
        }
    }

    @Override
    public String getName() {
        return subject.getPrincipal().getName();
    }

    public String getResponseXml() {
        return responseXml;
    }

    public ReportPortalSamlAuthentication setResponseXml(String responseXml) {
        this.responseXml = responseXml;
        return this;
    }

    public void setAuthorities(List<? extends GrantedAuthority> grantedAuthorities) {
        this.grantedAuthorities = grantedAuthorities;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}