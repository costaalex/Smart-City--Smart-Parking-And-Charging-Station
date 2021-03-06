package data_collector_and_manager.services;

import data_collector_and_manager.httpServer.DefaultSmartObjectDataManager;
import data_collector_and_manager.httpServer.ISmartObjectDataManager;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class AppConfig extends Configuration {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    private ISmartObjectDataManager smartObjectDataManager = null;

    public ISmartObjectDataManager getInventoryDataManager(){

        if(this.smartObjectDataManager == null)
            this.smartObjectDataManager = new DefaultSmartObjectDataManager();

        return this.smartObjectDataManager;
    }

}