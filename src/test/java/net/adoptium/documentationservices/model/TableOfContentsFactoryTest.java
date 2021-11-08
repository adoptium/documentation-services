package net.adoptium.documentationservices.model;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.adoptium.documentationservices.services.RepoService;

public class TableOfContentsFactoryTest {

    @Inject
    private RepoService repoService;
	
	private Path path= null;
	
	/*
	@BeforeAll
	public void init() {
		try {
			path= repoService.downloadRepositoryContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestPathValid() {
		//given
		
		//then
		Assertions.assertNotNull(path);
	}

	@Test
	public void testException() {
		//given
		TableOfContentsFactory factory= new TableOfContentsFactory();
		
		//then
		Assertions.assertDoesNotThrow(()-> {factory.build(path);});
	}
	
	@Test
	public void testEmpty() {
		//given
		TableOfContentsFactory factory= new TableOfContentsFactory();
		TableOfContents contents= null;
		try {
			contents= factory.build(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//then
		Assertions.assertNotNull(contents);
		Assertions.assertEquals(contents.getDocumentations().count(), 0);
	}
    */
}
