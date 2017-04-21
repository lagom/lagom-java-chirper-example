/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import javax.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
  private WebJarAssets webJarAssets;

  @Inject
  public Application(WebJarAssets webJarAssets) {
    this.webJarAssets = webJarAssets;
  }

  public Result index() {
    return ok(views.html.index.render(this.webJarAssets));
  }

  public Result userStream(String userId) {
    return ok(views.html.index.render(this.webJarAssets));
  }

  public Result circuitBreaker() {
    return ok(views.html.circuitbreaker.render(this.webJarAssets));
  }

}
