package com.appsby.ernest;

import model.CourseIdea;
import model.CourseIdeaDAO;
import model.NotFoundException;
import model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by Ernest on 6/14/2016.
 */
public class Main {

    private static final String FLASH_MESSAGE_KEY = "flash_message" ;

    public static void main(String[] args) {

        staticFileLocation("/public");
        CourseIdeaDAO dao = new SimpleCourseIdeaDAO();
        before((req, res) -> {
            if (req.cookie("username") != null) {
                req.attribute("username", req.cookie("username"));

            }
        });

        before("/ideas", (req, res) -> {
                    if (req.attribute("username") == null) {

                        //TODO: send message about redirect
                        setFlashMessage(req,"Please sign in first");
                        res.redirect("/");
                        halt(); //stops the req from hitting other routes
                    }
                }
        );

        get("/", (req, res) -> {
            Map<String, String> model = new HashMap<>();
            model.put("username", req.attribute("username"));
            model.put("flashMessage", captureFlashMessage(req));
            return new ModelAndView(model, "index.mustache");
        }, new HandlebarsTemplateEngine());


        get("/ideas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("ideas", dao.findAll());
            model.put("flashMessage", captureFlashMessage(req));
            return new ModelAndView(model, "ideas.mustache");
        }, new HandlebarsTemplateEngine());


    post("/sign-in",(req, res) -> {
        Map<String, String> model = new HashMap<>();
        String username = req.queryParams("username");
        res.cookie("username", username);
        res.redirect("/ideas");
        return null;
    });

       post("/ideas",(req, res) -> {
           String title = req.queryParams("title");
           CourseIdea courseIdea = new CourseIdea(title,req.attribute("username"));
           dao.add(courseIdea);
           res.redirect("/ideas");
           return null;
       });

       post("ideas/:slug/vote",(req, res) -> {
        CourseIdea idea = dao.findBySlug(req.params("slug"));
          boolean added = idea.addVoter(req.attribute("username"));
           if(added) {
               setFlashMessage(req, "Thanks for your vote");
           }

           else {
               setFlashMessage(req, "You already voted");
           }
           res.redirect("/ideas");
           return null;}
           );


     get("/ideas/:slug",(req, res) -> {
            Map<String,Object>model = new HashMap<>();
            model.put("idea", dao.findBySlug(req.params("slug")));
            return new ModelAndView(model, "idea-details.mustache");
     }, new HandlebarsTemplateEngine());

     exception(NotFoundException.class, (exc, req, res) -> {
         res.status(404);
         HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
         String html = engine.render(new ModelAndView(null, "not-found.mustache"));
         res.body(html);
     });
    }

    private static void setFlashMessage(Request req, String message) {
   req.session().attribute(FLASH_MESSAGE_KEY , message);
    }

    private static String getFlashMessage(Request req) {
    if(req.session(false) == null) {
        return null;
    }
        if(!req.session().attributes().contains(FLASH_MESSAGE_KEY)) {
            return null;
        }

        return (String) req.session().attribute(FLASH_MESSAGE_KEY);
    }

    private static String captureFlashMessage(Request req) {
        String message = getFlashMessage(req);
        if(message != null) {
            req.session().removeAttribute(FLASH_MESSAGE_KEY);
        }
        return message;
    }
}
