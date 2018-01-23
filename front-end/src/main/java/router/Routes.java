package router;

import akka.stream.Materializer;
import controllers.*;
import play.api.mvc.Handler;
import play.api.mvc.RequestHeader;
import play.mvc.Http;
import play.api.routing.Router;
import play.routing.RoutingDsl;
import scala.Option;
import scala.PartialFunction;
import scala.Tuple3;
import scala.collection.Seq;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webjars.play.RequireJS;
import org.webjars.play.WebJarAssets;

/**
 * We use the Java DSL router instead of the Play routes file, so that this project can also be built by Maven, which
 * as yet does not have a plugin for compiling Play routers.
 */
@Singleton
public class Routes implements Router {

    private final Application application;
    private final Assets assets;
    private final WebJarAssets webJars;
    private final RequireJS requireJs;
    private final Materializer materializer;

    private final RoutingDsl routingDsl;
    private final Router router;

    @Inject
    public Routes(Application application,
                  Assets assets,
                  WebJarAssets webJars,
                  RequireJS requireJs,
                  Materializer materializer,
                  RoutingDsl routingDsl) {
        this.application = application;
        this.assets = assets;
        this.webJars = webJars;
        this.requireJs = requireJs;
        this.materializer = materializer;

        this.routingDsl = routingDsl;
        this.router = buildRouter();
    }

    private Router buildRouter() {
        return this.routingDsl
                // Index
                .GET("/").routeTo(application::index)
                .GET("/signup").routeTo(application::index)
                .GET("/addFriend").routeTo(application::index)
                .GET("/users/:id").routeTo(application::userStream)
                .GET("/cb").routeTo(application::circuitBreaker)

                // Assets
                .GET("/webjars/_requirejs").routeAsync(() ->
                        requireJs.setup()
                                .asJava()
                                .apply(requestHeader())
                                .run(materializer)
                )
                .GET("/assets/*file").routeAsync((String file) ->
                        assets.at("/public", file, false)
                                .asJava()
                                .apply(requestHeader())
                                .run(materializer)
                )
                .GET("/webjars/*file").routeAsync((String file) ->
                        webJars.at(file)
                                .asJava()
                                .apply(requestHeader())
                                .run(materializer)
                )
                .build().asScala();
    }

    private Http.RequestHeader requestHeader() {
        return Http.Context.current().request();
    }

    @Override
    public PartialFunction<RequestHeader, Handler> routes() {
        return router.routes();
    }

    @Override
    public Seq<Tuple3<String, String, String>> documentation() {
        return router.documentation();
    }

    @Override
    public Router withPrefix(String prefix) {
        return router.withPrefix(prefix);
    }

    @Override
    public Option<Handler> handlerFor(RequestHeader request) {
        return router.handlerFor(request);
    }

    @Override
    public play.routing.Router asJava() {
        return router.asJava();
    }
}
