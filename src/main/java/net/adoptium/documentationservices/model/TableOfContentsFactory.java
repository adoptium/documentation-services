package net.adoptium.documentationservices.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import net.adoptium.documentationservices.services.RepoService;

public class TableOfContentsFactory {
	
	@Inject
	private RepoService repoService;
    
    public TableOfContents build() throws IOException {
    	List<Documentation> documentationList = new ArrayList<>();
		Path path= repoService.downloadRepositoryContent();
		File[] files= path.toFile().listFiles();
		if (files!=null) {
			for (File dir: files) {
				List<Document> documentList= new ArrayList<>();
				if (dir.isDirectory()) {
					File[] docFiles= dir.listFiles();
					if (docFiles!=null) {
						for (File doc: docFiles) {
							String name= doc.getName().toLowerCase();
							if (name.endsWith(".html")) {
								String code= name.substring(name.indexOf('_'),name.lastIndexOf('.'));
								documentList.add(new Document(dir.getName(),code));
							}
						}
					}
				}
				if (documentList.size()>0) {
					documentationList.add(new Documentation(dir.getName(), documentList));
				}
			}
		}
    	return new TableOfContents(documentationList);
    }
}
