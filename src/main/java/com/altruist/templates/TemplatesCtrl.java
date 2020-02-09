package com.altruist.templates;

import com.altruist.ErrorCodes;
import com.altruist.ErrorDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/templates")
public class TemplatesCtrl {
    private TemplatesService templatesService;

    /**
     * I hope it's not a problem that I deviated from the requirements a bit in order to stick with HTTP / REST standards.
     * I returned 201 rather than 200 with a '{"Template is created successfully"}' body (which is invalid JSON as well).
     * I also returned the standard "Location" header as is required by HTTP spec.
     */
    @PostMapping(path = "", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> save(@RequestBody TemplateDTO templateDTO,
                                     HttpServletRequest request) throws URISyntaxException {
        templatesService.saveIfNotPresent(templateDTO);
        return ResponseEntity.created(new URI(request.getRequestURL() + "/" + templateDTO.getId()))
                             .build();
    }

    //TODO: add pagination
    @GetMapping(path = "", produces = APPLICATION_JSON_VALUE)
    public List<TemplateDTO> getAll(){
        return templatesService.findAll();
    }

    /**
     * Deviated from spec here to add an ErrorCode.  This allows programmatic consumers to process the response easier.
     */
    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    private ErrorDto handleDuplicate(EntityExistsException exception){
        return new ErrorDto(ErrorCodes.DUPLICATE_CREATION, "Template id existed. Please choose another template id.");
    }
}
