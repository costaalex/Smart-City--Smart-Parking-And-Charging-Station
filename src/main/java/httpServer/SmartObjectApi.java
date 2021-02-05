package httpServer;

import com.codahale.metrics.annotation.Timed;
import dto.SmartObject;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import model.GpsLocationDescriptor;
import model.SmartObjectTypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.SmartObjectResource;
import services.AppConfig;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
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
    public Response getSmartObjects(@Context ContainerRequestContext req,
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
                smartObjectList = this.conf.getInventoryDataManager().getSmartObjectsList();
            else if(smartObjectType == SmartObjectTypeDescriptor.PARKING_LOT) {
                smartObjectList = this.conf.getInventoryDataManager().getSmartObjectsList(SmartObjectTypeDescriptor.PARKING_LOT);
            }
            else if(smartObjectType == SmartObjectTypeDescriptor.CHARGING_STATION) {
                smartObjectList = this.conf.getInventoryDataManager().getSmartObjectsList(SmartObjectTypeDescriptor.CHARGING_STATION);
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

    @GET
    @Path("/gps")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get all Gps Smart Objects Data")
    public Response getGpsData(@Context ContainerRequestContext req ){

        try {

            logger.info("Loading all requested Gps Smart Objects Data");

            Optional<Map<String, GpsLocationDescriptor>> gpsLocationDescriptor = this.conf.getInventoryDataManager().getSmartObjectLocationList();

            if(!gpsLocationDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Smart Objects Not Found !")).build();

            return Response.ok(gpsLocationDescriptor.get()).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @GET
    @Path("/{id_smart_object}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get Data about a Smart Objects")
    public Response getSmartObjectById(@Context ContainerRequestContext req,
                              @PathParam("id_smart_object") String idSmartObject) {

        try {

            logger.info("Loading Smart Object Info for id: {}", idSmartObject);

            //Check the request
            if(idSmartObject == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Smart Object Id Provided !")).build();

            Optional<SmartObject> smartObject = this.conf.getInventoryDataManager().getSmartObjectById(idSmartObject);

            if(!smartObject.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Smart Object Id Not Found !")).build();

            return Response.ok(smartObject.get()).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @GET
    @Path("/{id_smart_object}/{sensor_type}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get Data about a Sensor of a Smart Objects")
    public Response getDevice(@Context ContainerRequestContext req,
                              @PathParam("id_smart_object") String idSmartObject,
                              @PathParam("sensor_type") String sensor_type){

        try {

            logger.info("Loading Smart Object Info for id: {}", idSmartObject);

            //Check the request
            if(idSmartObject == null || sensor_type == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Smart Object Id Provided !")).build();

            Optional<SmartObject> smartObject = this.conf.getInventoryDataManager().getSmartObjectById(idSmartObject);

            if(!smartObject.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Smart Object Id Not Found !")).build();

            SmartObject extractedSmartObject = smartObject.get();
            SmartObjectResource<?> sensor = extractedSmartObject.getResourceMap().get(sensor_type);                  //Extract the requested sensor

            if (sensor != null)
                return Response.ok(sensor).build();
            else
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Sensor type Not Found !")).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @PUT
    @Path("/{location_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Update an existing Location")
    public Response updateLocation(@Context ContainerRequestContext req,
                                   @Context UriInfo uriInfo,
                                   @PathParam("location_id") String locationId,
                                   LocationUpdateRequest locationUpdateRequest) {

        try {

            logger.info("Incoming Location ({}) Update Request: {}", locationId, locationUpdateRequest);

            //Check if the request is valid
            if(locationUpdateRequest == null || !locationUpdateRequest.getId().equals(locationId))
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request ! Check Location Id")).build();

            //Check if the device is available and correctly registered otherwise a 404 response will be sent to the client
            if(!this.conf.getInventoryDataManager().getLocation(locationId).isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Location not found !")).build();

            LocationDescriptor locationDescriptor = (LocationDescriptor) locationUpdateRequest;
            this.conf.getInventoryDataManager().updateLocation(locationDescriptor);

            return Response.noContent().build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

}
