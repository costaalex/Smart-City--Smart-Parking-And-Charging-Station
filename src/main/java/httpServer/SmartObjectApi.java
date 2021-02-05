package httpServer;

import com.codahale.metrics.annotation.Timed;
import dto.SmartObject;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import model.SmartObjectTypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.AppConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

@Path("/smartcity")
@Api("Smart Object Data")
public class SmartObjectApi {

    final protected Logger logger = LoggerFactory.getLogger(SmartObjectApi.class);

    final AppConfig conf;

    public SmartObjectApi(AppConfig conf) {
        this.conf = conf;
    }

    @GET
    @Path("/")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get all available Smart Objects")
    public Response getLocations(@Context ContainerRequestContext req,
                                 @QueryParam("type") String type) {

        try {

            SmartObjectTypeDescriptor smartObjectType = null;

            if (type != null) {
                if (type.equals("parking_lot"))
                    smartObjectType = SmartObjectTypeDescriptor.PARKING_LOT;
                else if (type.equals("charging_station"))
                    smartObjectType = SmartObjectTypeDescriptor.CHARGING_STATION;
            }

            logger.info("Loading all requested Smart Objects");

            Optional<Map<String, SmartObject>> smartObjectList = null;

            //No filter applied
            if(smartObjectType == null)
                smartObjectList = this.conf.getInventoryDataManager().getSmartObjectList();
            else if(smartObjectType == SmartObjectTypeDescriptor.PARKING_LOT) {
                smartObjectList = this.conf.getInventoryDataManager().getSmartObjectList();
            }
            else if(smartObjectType == SmartObjectTypeDescriptor.CHARGING_STATION) {
                smartObjectList = this.conf.getInventoryDataManager().getSmartObjectList();
            }
            else
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Type is not valid !")).build();

            if(smartObjectList == null)
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Smart Objects Not Found !")).build();

            return Response.ok(smartObjectList).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }



}
