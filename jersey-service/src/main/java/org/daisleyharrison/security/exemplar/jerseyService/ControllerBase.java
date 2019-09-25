package org.daisleyharrison.security.samples.jerseyService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;

import javax.management.ServiceNotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.ProfileServiceProvider;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.samples.jerseyService.filters.UserClaimsPrincipal;
import org.daisleyharrison.security.samples.jerseyService.models.ErrorResponse;
import org.daisleyharrison.security.samples.jerseyService.models.ViewContext;
import org.daisleyharrison.security.samples.jerseyService.utilities.HandlebarsHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerBase {
    private static Logger LOGGER = LoggerFactory.getLogger(ControllerBase.class);
    private static Handlebars s_handlebars;

    private static String _defaultErrorTemplate = "<html><head><title>c2id - Error</title></head><body><div>{{message}}</div><div>{{type}}</div><div>{{error}}</div></body></html>";

    @Context
    private SecurityContext securityContext;

    @Context
    public UriInfo uriInfo;

    protected AuthClaims getUserClaims() {
        if (securityContext != null) {
            Principal principal = securityContext.getUserPrincipal();
            if (principal instanceof UserClaimsPrincipal) {
                UserClaimsPrincipal sessionPrincipal = (UserClaimsPrincipal) principal;
                return sessionPrincipal.getUserClaims();
            }
        }
        return null;
    }

    protected ViewContext getContext() {
        return new ViewContext(this.getUserClaims(), this.uriInfo.getPath());
    }



    public static String getErrorResponseText(String message, Exception exception) {
        return _defaultErrorTemplate.replace("{{message}}", message).replace("{{type}}", exception.getClass().getName())
                .replace("{{error}}", exception.getMessage());
    }

    public Response errorResponse(int status, String error, String error_description) {
        return Response.status(status).entity(new ErrorResponse(error, error_description)).build();
    }

    public Response errorResponse(int status, String error) {
        return Response.status(status).entity(new ErrorResponse(error, error)).build();
    }

    public Response errorResponse(Throwable throwable) {
        ErrorResponse content = new ErrorResponse(throwable.getClass().getName(), throwable.getMessage());
        return Response.status(500).entity(content).build();
    }

    public Response errorResponse(String error, Throwable exception) {
        return Response.status(500).entity(new ErrorResponse(error, exception.getMessage())).build();
    }

    private static Handlebars createHandlebars() {
        TemplateLoader loader = null;
        try {
            ConfigurationServiceProvider config = Main.getConfigurationService();
            if (config.hasProperty("handlebars.classLoader")) {
                ClassPathTemplateLoader classLoader = new ClassPathTemplateLoader();
                String prefix = config.getValue("handlebars.classLoader.prefix", FileTemplateLoader.DEFAULT_PREFIX);
                String suffix = config.getValue("handlebars.classLoader.suffix", TemplateLoader.DEFAULT_SUFFIX);
                classLoader.setPrefix(prefix);
                classLoader.setSuffix(suffix);
                loader = classLoader;
            } else if (config.hasProperty("handlebars.fileLoader")) {
                String basedir = config.getValue("handlebars.fileLoader.baseDir", ".");
                FileTemplateLoader fileLoader = new FileTemplateLoader(new File(basedir));
                String prefix = config.getValue("handlebars.fileLoader.prefix", FileTemplateLoader.DEFAULT_PREFIX);
                String suffix = config.getValue("handlebars.fileLoader.suffix", FileTemplateLoader.DEFAULT_SUFFIX);
                fileLoader.setPrefix(prefix);
                fileLoader.setSuffix(suffix);
                loader = fileLoader;
            }
        } catch (ServiceNotFoundException exception) {
        }
        if (loader == null) {
            loader = new FileTemplateLoader(new File("."));
        }
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(HandlebarsHelpers.class);
        return handlebars;
    }

    public static Handlebars getHandlebars() {
        if (s_handlebars == null) {
            s_handlebars = createHandlebars();
        }
        return s_handlebars;
    }

    protected ConfigurationServiceProvider getConfigurationService() throws ServiceNotFoundException {
        return Main.getConfigurationService();
    }

    protected TokenizerServiceProvider getTokenizerService() throws ServiceNotFoundException {
        return Main.getTokenizerService();
    }

    protected ProfileServiceProvider getProfileService() throws ServiceNotFoundException {
        return Main.getProfileService();
    }

    public static Template getTemplate(String templateName) throws IOException {
        Template template = getHandlebars().compile(templateName);
        return template;
    }

    public Template template(String templateName) throws IOException {
        return getTemplate(templateName);
    }

    public String template(String templateName, Object model) {
        try {
            return template(templateName).apply(model);
        } catch (FileNotFoundException exception) {
            throw new WebApplicationException(Response.status(404).build());
        } catch (HandlebarsException exception) {
            LOGGER.info(exception.getMessage());
            throw new WebApplicationException(Response.status(404).build());
        } catch (IOException exception) {
            return getErrorResponseText("An error occurred loading the login template", exception);
        }
    }

    public ResponseBuilder templateResponseBuilder(String templateName, Object model) {
        return Response.ok().entity(template(templateName, model));
    }
}