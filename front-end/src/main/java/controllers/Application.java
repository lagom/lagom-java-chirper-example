/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import javax.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

import org.webjars.play.WebJarsUtil;

public class Application extends Controller {
  private WebJarsUtil webJarAssetsUtil;

  @Inject
  public Application(WebJarsUtil webJarAssets) {
    this.webJarAssetsUtil = webJarAssets;
  }

  public Result index() {
    return ok(views.html.index.render(this.webJarAssetsUtil));
  }

  public Result userStream(String userId) {
    return ok(views.html.index.render(this.webJarAssetsUtil));
  }

  public Result circuitBreaker() {
    return ok(views.html.circuitbreaker.render(this.webJarAssetsUtil));
  }

}
