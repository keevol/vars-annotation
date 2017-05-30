package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.AuthService;
import org.mbari.m3.vars.annotation.services.BasicJWTAuthService;
import org.mbari.m3.vars.annotation.services.TestConfig;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian Schlining
 * @since 2017-05-23T10:07:00
 */
public class AnnoServiceTest {

    AnnoWebServiceFactory serviceGenerator = TestConfig.ANNO_SERVICE_GEN;
    AuthService authService = new BasicJWTAuthService(serviceGenerator,
            new Authorization("APIKEY", ""));
    AnnoService annoService = new AnnoService(serviceGenerator, authService);
    Duration timeout = Duration.ofMillis(15000);

    private final UUID uuid = UUID.fromString("ccbe1c1b-100d-41ab-87ac-7e48d57c8278");
    private final UUID tempUuid = UUID.randomUUID();

    @Test
    public void testFindAnnotatons() {

        // Find all
        CompletableFuture<List<Annotation>> f0 = annoService.findAnnotations(uuid);
        Optional<List<Annotation>> annos0 = await(f0, timeout);
        assertTrue("Expect to find annotations, but none were returned", annos0.isPresent());
        List<Annotation> as0 = annos0.get();
        assertFalse("Expect to find annotations, but none were returned", as0.isEmpty());

        // Find using limit and offset
        CompletableFuture<List<Annotation>> f1 = annoService.findAnnotations(uuid, 2L, 4L);
        Optional<List<Annotation>> annos1 = await(f1, timeout);
        assertTrue("Expect to find annotations, but none were returned", annos1.isPresent());
        List<Annotation> as1 = annos1.get();
        assertFalse("Expect to find annotations, but none were returned", as1.isEmpty());
        assertTrue("Expected 2 annotations but found " + as1.size(), as1.size() == 2);

    }

    @Test
    public void testCountAnnotations() {
        CompletableFuture<AnnotationCount> f0 = annoService.countAnnotations(uuid);
        Optional<AnnotationCount> c0 = await(f0, timeout);
        assertTrue("Expected a count, but nothing was returned", c0.isPresent());
        AnnotationCount i = c0.get();
        assertTrue("Expected 77 but found " + i, i.getCount() == 77);
    }

    @Test
    public void testCrudAnnotation() {
        // Create
        Annotation annotation = new Annotation();
        annotation.setVideoReferenceUuid(tempUuid);
        annotation.setConcept("Nanomia bijuga");
        annotation.setObserver("brian");
        annotation.setRecordedTimestamp(Instant.now());
        CompletableFuture<Annotation> f = annoService.createAnnotation(annotation);
        Optional<Annotation> annoOpt0 = await(f, timeout);
        assertTrue("Annotation return value was null", annoOpt0.isPresent());
        Annotation anno0 = annoOpt0.get();
        assertNotNull("Annotation videoReferenceUuid was missing", anno0.getVideoReferenceUuid());
        assertNotNull("Annotation imagedMomentUuid was missing", anno0.getImagedMomentUuid());
        assertNotNull("Annotation observationUuid was missing", anno0.getObservationUuid());
        compareAnnotations(annotation, anno0, false);

        // Update
        anno0.setGroup("ROV");
        anno0.setConcept("Pandalus platyceros");
        Optional<Annotation> annoOpt1 = await(annoService.updateAnnotation(anno0), timeout);
        Annotation anno1 = annoOpt1.get();
        compareAnnotations(anno0, anno1, true);


        //Delete
        await(annoService.deleteAnnotation(anno0.getObservationUuid()), timeout);
        Optional<Annotation> annoOpt = await(annoService.findByUuid(anno0.getObservationUuid()), timeout);
        assertFalse("Annotation was not deleted", annoOpt.isPresent());
    }

    private void compareAnnotations(Annotation a0, Annotation a1, boolean compareUuid) {
        if (compareUuid) {
            assertEquals("ObservationUuid were not equal", a0.getObservationUuid(), a1.getObservationUuid());
            assertEquals("VideoReferenceUuid were not equal", a0.getVideoReferenceUuid(), a1.getVideoReferenceUuid());
            assertEquals("ImageMomentUuid were not equal", a0.getImagedMomentUuid(), a1.getImagedMomentUuid());
        }
        assertEquals("Concept was not as expected", a0.getConcept(), a1.getConcept());
        assertEquals("Observer was not as expected", a0.getObserver(), a1.getObserver());
        assertEquals("RecordedTimestamp was not as expected", a0.getRecordedTimestamp(), a1.getRecordedTimestamp());
        assertNotEquals("ObservationTimestamp were equal ... not OK", a0.getObservationTimestamp(), a1.getObservationTimestamp());
        assertEquals("Activity were not equal", a0.getActivity(), a1.getActivity());
        assertEquals("Group was not equal", a0.getGroup(), a1.getGroup());
    }

    @Test
    public void testCreateAndDeleteAssociation() {

        // --- 1. create annotation
        Annotation annotation = new Annotation();
        annotation.setVideoReferenceUuid(tempUuid);
        annotation.setConcept("Nanomia bijuga");
        annotation.setObserver("brian");
        annotation.setRecordedTimestamp(Instant.now());
        // Insert annotation into database
        CompletableFuture<Annotation> f = annoService.createAnnotation(annotation);
        Optional<Annotation> annoOpt0 = await(f, timeout);
        assertTrue("Annotation was not created", annoOpt0.isPresent());
        Annotation anno = annoOpt0.get();

        // --- 2. Create association
        Association association = new Association("eating", "Sergestes",
                null, "text/plain");
        // Insert association into database
        CompletableFuture<Association> f1 = annoService.createAssociation(anno.getObservationUuid(),
                association);
        Optional<Association> assOpt0 = await(f1, timeout);
        assertTrue("Association is missing", assOpt0.isPresent());
        Association ass0 = assOpt0.get();
        assertEquals(association.getLinkName(), ass0.getLinkName());
        assertEquals(association.getToConcept(), ass0.getToConcept());
        assertEquals("nil", ass0.getLinkValue());
        // Find parent association in database and check that it has one association
        Optional<Annotation> annoOpt1 = await(annoService.findByUuid(anno.getObservationUuid()), timeout);
        assertTrue("Annotation is missing", annoOpt1.isPresent());
        Annotation anno1 = annoOpt1.get();
        assertEquals("Annotation should have 1 association. Association is not in database",
                1, anno1.getAssociations().size());

        // Update
        Association ass1 = new Association("swimming", "self", "nil", "text/plain",
                ass0.getUuid());
        Optional<Association> assOpt2 = await(annoService.updateAssociation(ass1), timeout);
        assertTrue("Association is missing", assOpt0.isPresent());
        Association ass2 = assOpt2.get();
        assertEquals(ass1.getLinkName(), ass2.getLinkName());
        assertEquals(ass1.getToConcept(), ass2.getToConcept());
        assertEquals(ass1.getLinkValue(), ass2.getLinkValue());

        // Delete association from database
        await(annoService.deleteAssociation(ass0.getUuid()), timeout);
        // Find parent association again and check that it has no associations
        Optional<Annotation> annoOpt2 = await(annoService.findByUuid(anno.getObservationUuid()), timeout);
        assertTrue("Annotation is missing", annoOpt2.isPresent());
        Annotation anno2 = annoOpt2.get();
        assertTrue("Association was not deleted from database", anno2.getAssociations().isEmpty());
        // Delete parent annotaiton from database
        await(annoService.deleteAnnotation(anno.getObservationUuid()), timeout);

    }

    @Test
    public void testCrudImages() throws UnsupportedEncodingException, MalformedURLException {
        // --- 1. create annotation
        Annotation annotation = new Annotation();
        annotation.setVideoReferenceUuid(tempUuid);
        annotation.setConcept("Nanomia bijuga");
        annotation.setObserver("brian");
        annotation.setRecordedTimestamp(Instant.now());
        // Insert annotation into database
        CompletableFuture<Annotation> f = annoService.createAnnotation(annotation);
        Optional<Annotation> annoOpt0 = await(f, timeout);
        assertTrue("Annotation was not created", annoOpt0.isPresent());
        Annotation anno = annoOpt0.get();

        // --- 2. Create Image
        Image image = new Image();
        image.setVideoReferenceUuid(annotation.getVideoReferenceUuid());
        image.setRecordedTimestamp(annotation.getRecordedTimestamp());
        String urlS = "http://www.mbari.org/" + URLEncoder.encode(Instant.now().toString(),"UTF-8");
        image.setUrl(new URL(urlS));
        Optional<Image> imgOpt0 = await(annoService.createImage(image), timeout);
        assertTrue("Image is missing", imgOpt0.isPresent());
        Image img0 = imgOpt0.get();
        assertEquals(image.getVideoReferenceUuid(), img0.getVideoReferenceUuid());
        assertEquals(image.getRecordedTimestamp(), img0.getRecordedTimestamp());
        assertEquals(image.getUrl(), img0.getUrl());

        // --- 3. Delete Image
        await(annoService.deleteImage(img0.getImageReferenceUuid()), timeout);
        Optional<List<Annotation>> imgOpt1 = await(annoService.findByImageReference(img0.getImageReferenceUuid()), timeout);
        assertFalse("Image was still in database after delete", imgOpt0.isPresent());

        // Cleanup and delete original annotation
        await(annoService.deleteAnnotation(anno.getObservationUuid()), timeout);


    }


    private <T> Optional<T> await(CompletableFuture<T> f, Duration timeout) {
        Optional<T> r;
        try {
            r = Optional.ofNullable(f.get(timeout.toMillis(), TimeUnit.MILLISECONDS));
        }
        catch (Exception e) {
            r = Optional.empty();
        }
        return r;
    }





}