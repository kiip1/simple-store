package nl.kiipdevelopment.simplestore.persistence;

import nl.kiipdevelopment.simplestore.Store;

import java.nio.file.Path;

public interface StorageMethod<T> {
	Store<T> store();

	Path location();

	void save();

	void load();
}
