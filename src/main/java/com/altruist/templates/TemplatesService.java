package com.altruist.templates;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
                log.warn(String.format("Attempt to overwrite template: '%s'.", t.getId()));
                throw new EntityExistsException(String.format("Template ID '%s' already exists.", t.getId()));
            });
        log.debug(String.format("Saving template: '%s'.", templateDTO.toString()));
        repo.save(toDomain(templateDTO));
    }

    public List<TemplateDTO> findAll() {
        return StreamSupport.stream(repo.findAll()
                                        .spliterator(), true)
                            .map(this::toDTO)
                            .collect(Collectors.toList());
    }

    private Template toDomain(TemplateDTO templateDTO) {
        Template template = new Template();
        template.setId(templateDTO.getId());
        template.setText(templateDTO.getText());
        return template;
    }

    private TemplateDTO toDTO(Template template) {
        return new TemplateDTO(template.getId(), template.getText());
    }
}
