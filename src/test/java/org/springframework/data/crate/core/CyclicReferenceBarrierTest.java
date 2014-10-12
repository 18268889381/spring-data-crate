package org.springframework.data.crate.core;

import static java.util.Collections.singleton;
import static org.springframework.data.crate.core.CyclicReferenceBarrier.cyclicReferenceBarrier;

import java.util.List;

import org.junit.Test;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;

public class CyclicReferenceBarrierTest {

	@Test(expected=CyclicReferenceException.class)
	public void shouldDetectSelfReferencingCycle() {
		
		CyclicReferenceBarrier barrier = cyclicReferenceBarrier();
		
		CratePersistentEntity<?> entity = prepareMappingContext(SelfCyclic.class).
										  getPersistentEntity(SelfCyclic.class);
		
		CratePersistentProperty property = entity.getEntityProperties().iterator().next();
		
		barrier.guard(property);
		barrier.guard(property);
	}
	
	@Test(expected=CyclicReferenceException.class)
	public void shouldDetectSelfReferencingCycleViaCollection() {
		
		CyclicReferenceBarrier barrier = cyclicReferenceBarrier();
		
		CratePersistentEntity<?> entity = prepareMappingContext(SelfCyclicViaCollection.class).
										  getPersistentEntity(SelfCyclicViaCollection.class);
		
		CratePersistentProperty property = entity.getCollectionProperties().iterator().next();
		
		barrier.guard(property);
		barrier.guard(property);
	}
	
	@Test(expected=CyclicReferenceException.class)
	public void shouldDetectSelfReferencingCycleViaArray() {
		
		CyclicReferenceBarrier barrier = cyclicReferenceBarrier();
		
		CratePersistentEntity<?> entity = prepareMappingContext(SelfCyclicViaArray.class).
										  getPersistentEntity(SelfCyclicViaArray.class);
		
		CratePersistentProperty property = entity.getCollectionProperties().iterator().next();
		
		barrier.guard(property);
		barrier.guard(property);
	}
	
	@Test(expected=CyclicReferenceException.class)
	public void shouldDetectNestedReferencingCycle() {
		
		CrateMappingContext mappingContext = prepareMappingContext(CyclicLevelOne.class);
		
		CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(CyclicLevelOne.class);
		
		crawlEntity(mappingContext, entity, cyclicReferenceBarrier());
	}
	
	@Test(expected=CyclicReferenceException.class)
	public void shouldDetectInBetweenReferencingCycle() {
		
		CrateMappingContext mappingContext = prepareMappingContext(InBetweenCycling.class);
		
		CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(InBetweenCycling.class);
		
		crawlEntity(mappingContext, entity, cyclicReferenceBarrier());
	}
	
	@Test(expected=CyclicReferenceException.class)
	public void shouldDetectCycleWithSameClassNameButDifferentPackage() {
		
		CrateMappingContext mappingContext = prepareMappingContext(CyclicBook.class);
		
		CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(CyclicBook.class);
		
		crawlEntity(mappingContext, entity, cyclicReferenceBarrier());
	}
	
	@Test
	public void shouldNotCycleWithSameClassNameButDifferentPackage() {
		
		CrateMappingContext mappingContext = prepareMappingContext(Book.class);
		
		CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(Book.class);
		
		crawlEntity(mappingContext, entity, cyclicReferenceBarrier());
	}
	
	private void crawlEntity(CrateMappingContext mappingContext, CratePersistentEntity<?> entity, CyclicReferenceBarrier barrier) {
		
		for(CratePersistentProperty property : entity.getEntityProperties()) {
			barrier.guard(property);
			crawlEntity(mappingContext, mappingContext.getPersistentEntity(property), barrier);
		}
	}
	
	private static CrateMappingContext prepareMappingContext(Class<?> type) {

		CrateMappingContext mappingContext = new CrateMappingContext();
		mappingContext.setInitialEntitySet(singleton(type));
		mappingContext.initialize();

		return mappingContext;
	}
	
	static class SelfCyclic {
		SelfCyclic cyclic;
	}
	
	static class SelfCyclicViaCollection {
		List<SelfCyclic> selfCyclics;
	}
	
	static class SelfCyclicViaArray {
		List<SelfCyclicViaArray> selfCyclics;
	}
	
	static class CyclicLevelOne {
		CyclicLevelTwo two;
	}
	
	static class CyclicLevelTwo {
		CyclicLevelThree three;
	}
	
	static class CyclicLevelThree {
		CyclicLevelOne cyclic;
	}
	
	static class InBetweenCycling {
		CyclicLevelOne cycleStart;
	}
	
	public static class Book {
		org.springframework.data.sample.entities.Book book;
	}
	
	public static class CyclicBook {
		org.springframework.data.sample.entities.CyclicBook cyclicBook;
	}
}