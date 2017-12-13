package com.capitalone.dashboard.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitalone.dashboard.misc.HygieiaException;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.model.Pod;
import com.capitalone.dashboard.model.deploy.DeployDataResponse;
import com.capitalone.dashboard.model.deploy.NewEnvironment;
import com.capitalone.dashboard.request.DeployDataCreateRequest;
import com.capitalone.dashboard.service.NewDeployService;

@CrossOrigin(origins = "http://cdlatezlm01ap2:3005")
@RestController
public class DeployController {


    private final NewDeployService deployService;

    @Autowired
    public DeployController(NewDeployService deployService) {
        this.deployService = deployService;
    }
   
    @RequestMapping(value = "/deploy/all/{date}",method = GET, produces = APPLICATION_JSON_VALUE)
    public List<DeployDataResponse> getDeploy(@PathVariable long date){
    return deployService.getDeployStatus(date);	
    }
    
    @RequestMapping(value = "/deploy/pods" , method = GET, produces = APPLICATION_JSON_VALUE)
    public Iterable<Pod> getPods(){
    	return deployService.getPods();
    }
    @RequestMapping(value = "/deploy/status/{componentId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public DataResponse<List<NewEnvironment>> deployStatus(@PathVariable ObjectId componentId) {
        return deployService.getDeployStatus(componentId);
    }

    @RequestMapping(value = "/deploy", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createBuild(@Valid @RequestBody DeployDataCreateRequest request) throws HygieiaException {
        String response = deployService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
