package org.jooby.sse.heatmap;

import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.typesafe.config.Config;
import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.assets.Assets;
import org.jooby.csl.XSS;
import org.jooby.jade.Jade;
import org.jooby.json.Gzon;
import org.jooby.metrics.Metrics;
import org.jooby.pac4j.Auth;
import org.jooby.raml.Raml;
import org.jooby.sitemap.Sitemap;
import org.jooby.sse.heatmap.auth.MyUserPasswdAuthenticator;
import org.jooby.sse.heatmap.dao.DB;
import org.jooby.sse.heatmap.domain.Location;
import org.jooby.swagger.SwaggerUI;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
* Application
*
* @author rjain
* */
public class App extends Jooby {


    {

        use(new Assets());
        use(new Gzon());
        use(new Sitemap());
        use(new Jade());
        use(new XSS());
        use(new Auth().form("*", MyUserPasswdAuthenticator.class).logout("/logout", "/login"));

        get("/heatmap", req -> {
            Config config = req.require(Config.class);
            return Results.html("heatmap").put("GOOGLE_API_KEY", config.getString("googleMapAPIKey"));
        });

        use(new Metrics()
                .request()
                .threadDump()
                .ping()
                // .healthCheck("db", new DatabaseHealthCheck())
                .metric("memory", new MemoryUsageGaugeSet())
                .metric("threads", new ThreadStatesGaugeSet())
                .metric("gc", new GarbageCollectorMetricSet())
                .metric("fs", new FileDescriptorRatioGauge())
        );

        /**
         *
         * Everything about your Locations.
         */
        use("/api/locations")
                /**
                 *
                 * List Locations ordered by id.
                 *
                 * @param start Start offset, useful for paging. Default is <code>0</code>.
                 * @param max Max page size, useful for paging. Default is <code>50</code>.
                 * @return Pets ordered by name.
                 */
                .get(req -> {
                    int start = req.param("start").intValue(0);
                    int max = req.param("max").intValue(50);
                    DB db = req.require(DB.class);
                    List<Location> locations = db.findAll(start, max);
                    return locations;
                })
                /**
                 *
                 * Find Location by ID
                 *
                 * @param id Location ID.
                 * @return Returns <code>200</code> with a single pet or <code>404</code>
                 */
                .get("/:id", req -> {
                    int id = req.param("id").intValue();
                    DB db = req.require(DB.class);
                    Location location = db.find(id);
                    if (location == null) {
                        throw new Err(Status.NOT_FOUND);
                    }
                    return location;
                })
                /**
                 *
                 * Add a new Location to the store.
                 *
                 * @param body Location object that needs to be added to the store.
                 * @return Returns a saved pet.
                 */
                .post(req -> {
                    Location location = req.body().to(Location.class);
                    DB db = req.require(DB.class);
                    db.save(location);
                    return location;
                })
                /**
                 *
                 * Update an existing Location.
                 *
                 * @param body Location object that needs to be updated.
                 * @return Returns a saved Location.
                 */
                .put(req -> {
                    Location location = req.body().to(Location.class);
                    DB db = req.require(DB.class);
                    db.save(location);
                    return location;
                })
                /**
                 *
                 * Deletes a Locations by ID.
                 *
                 * @param id Location ID.
                 * @return A <code>204</code>
                 */
                .delete("/:id", req -> {
                    int id = req.param("id").intValue();
                    DB db = req.require(DB.class);
                    db.delete(id);
                    return Results.noContent();
                })
                .produces("json")
                .consumes("json");

        {
            // creates an executor service
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            //DB db = new DB();

            sse("/api/events", sse -> {

                // if we go down, recover from last event ID we sent. Otherwise, start from zero.
                int lastId = sse.lastEventId(Integer.class).orElse(0);
                AtomicInteger next = new AtomicInteger(lastId);
                //todo: one time initialization
                DB db = sse.require(DB.class);
                // send events every 60s
                ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
                    Integer id = next.incrementAndGet();
                    db.save(id, "Location-" + id);
                    Object data = db.find(id);
                    // send data and idhome
                    sse.event(data).id(id).type("json").send();
                }, 0, 200, TimeUnit.MILLISECONDS);

                // on connection lost, cancel 60s task
                sse.onClose(() -> future.cancel(true));
            });

        }

        new Raml()
                .install(this);

        new SwaggerUI()
                .tag(route -> "locations")
                .install(this);
        // Default Path
        get("/*", req -> Results.html("home"));

    }

    public static void main(final String[] args) throws Exception {
        new App().start(args);
    }

}