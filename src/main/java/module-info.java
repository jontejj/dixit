module com.github.jontejj.dixit
{
	exports com.github.jontejj.dixit;

	// requires java.annotation;
	requires flow.server;
	requires spring.boot;
	requires spring.boot.autoconfigure;
	requires spring.boot.starter.web;
	requires spring.web;
	requires spring.beans;
	requires vaadin.button.flow;
	requires flow.html.components;
	requires vaadin.text.field.flow;
	requires vaadin.ordered.layout.flow;
	requires vaadin.notification.flow;
	requires spring.context;
	requires com.google.common;
	requires com.google.api.client;
	// requires javax.servlet.api;
}
