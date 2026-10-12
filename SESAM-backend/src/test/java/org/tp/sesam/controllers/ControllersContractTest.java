package org.tp.sesam.controllers;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;
import org.tp.sesam.controllers.handlers.GlobalExceptionHandler;
import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.models.enums.Role;
import org.tp.sesam.services.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests de la couche HTTP avec services simulés : ne prouvent pas les règles métier ou la sécurité. */
class ControllersContractTest {
    private final User actor = actor();
    private static User actor() {
        User user = new User();
        user.setId(42L);
        user.setRole(Role.FORMATEUR);
        return user;
    }
    private MockMvc mvc(Object controller) {
        HandlerMethodArgumentResolver identity = new HandlerMethodArgumentResolver() {
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }
            public Object resolveArgument(MethodParameter p, ModelAndViewContainer m,
                    NativeWebRequest r, org.springframework.web.bind.support.WebDataBinderFactory b) {
                return actor;
            }
        };
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(identity, new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test void suppressionDesReglesUtiliseLIdentiteConnectee() throws Exception {
        RegleService service = mock(RegleService.class);
        mvc(new RegleController(service)).perform(delete("/api/v1/rules/rooms/7"))
                .andExpect(status().isNoContent());
        verify(service).supprimerToutesSalle(7L,42L);
        verifyNoMoreInteractions(service);
    }
    @Test void affectationRetourne204SeulementApresLeService() throws Exception {
        MaterielService service = mock(MaterielService.class);
        mvc(new MaterielController(service,new MaterielDtoAssembler(),mock(ReinstallationService.class)))
                .perform(post("/api/v1/materials/assignments").contentType("application/json")
                    .content("{\"materialIds\":[1,2],\"roomId\":7}"))
                .andExpect(status().isNoContent());
        verify(service).affecter(new AffectationMateriels(List.of(1L,2L),7L),42L);
    }
    @Test void affectationVideEstRefuseeAvantLeService() throws Exception {
        MaterielService service = mock(MaterielService.class);
        mvc(new MaterielController(service,new MaterielDtoAssembler(),mock(ReinstallationService.class)))
                .perform(post("/api/v1/materials/assignments").contentType("application/json")
                    .content("{\"materialIds\":[],\"roomId\":7}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void reinstallationExposeSonAdresseDeSuivi() throws Exception {
        ReinstallationService service = mock(ReinstallationService.class);
        when(service.lancerSalle(7L,42L)).thenReturn(new ReinstallationResponse(99L,"PENDING",List.of(1L)));
        mvc(new SalleController(mock(SalleService.class),new SalleDtoAssembler(),service))
                .perform(post("/api/v1/rooms/7/reinstallations"))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location","/api/v1/reinstallations/99"));
    }
    @Test void traitementAbsentEstAnnonce501() throws Exception {
        DashboardService service = new DashboardService() { };
        mvc(new DashboardController(service)).perform(get("/api/v1/dashboard"))
                .andExpect(status().isNotImplemented());
    }
    @Test void ressourceImportInconnueEstRefusee() throws Exception {
        TransfertService service = mock(TransfertService.class);
        mvc(new ImportController(service)).perform(post("/api/v1/imports/passwords")
                .contentType("application/json").content("{\"externalIds\":[\"A1\"]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void descriptionVideAutoriseeMais151CaracteresRefuses() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new AlerteCreation("PANNE","",List.of(1L))).isEmpty());
            assertFalse(validator.validate(new AlerteCreation("PANNE","x".repeat(151),List.of(1L))).isEmpty());
        }
    }
    @Test void disponibiliteNecessiteUnePeriodeNonAmbigue() {
        assertFalse(new SalleFilter(null,null,null,null,true,null,null,null).isPeriodeValide());
        assertTrue(new SalleFilter(null,null,null,null,true,1L,null,null).isPeriodeValide());
        assertFalse(new SalleFilter(null,null,null,null,true,1L,LocalDate.of(2026,1,1),
                LocalDate.of(2026,1,2)).isPeriodeValide());
    }
    @Test void patchVideRefuseEtFalseResteUneValeurValide() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertFalse(validator.validate(new StagePatch(0L,null,null,null,null)).isEmpty());
            assertTrue(validator.validate(new ReglePatch(false)).isEmpty());
            assertFalse(validator.validate(new ReglePatch(null)).isEmpty());
        }
    }
}
