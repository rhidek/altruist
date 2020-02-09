package com.altruist.templates;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@Transactional
@Service
public class TemplatesService {
    private TemplateRepo repo;

    public void saveIfNotPresent(TemplateDTO templateDTO) throws EntityExistsException { // explicit throws in signature as a form of documentation
        Objects.requireNonNull(templateDTO);
        repo.findById(templateDTO.getId())
            .ifPresent(t -> {
                log.warn(String.format("Attempt to overwrite template: '%s'.", t.id));
                throw new EntityExistsException(String.format("Template ID '%s' already exists.", t.id));
            });
        log.debug(String.format("Saving template: '%s'.", templateDTO.toString()));
        repo.save(toDomain(templateDTO));
    }

    private Template toDomain(TemplateDTO templateDTO) {
        Template template = new Template();
        template.id = templateDTO.getId();
        template.text = templateDTO.getText();
        return template;
    }
}
