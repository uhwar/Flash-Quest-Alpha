package com.flashquest.service;

import com.flashquest.model.DifficultyLevel;
import com.flashquest.model.Flashcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for creating and managing default Java flashcard content.
 * Implements the sample flashcards specified in the FlashQuest Agent Rules.
 */
public class DefaultFlashcardService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFlashcardService.class);

    /**
     * Creates the complete set of default Java flashcards.
     * @return List of default flashcards covering all Java categories
     */
    public static List<Flashcard> createDefaultFlashcards() {
        logger.info("Creating default Java flashcard dataset");
        
        List<Flashcard> flashcards = new ArrayList<>();
        
        // Add flashcards for each category
        flashcards.addAll(createJavaBasicsFlashcards());
        flashcards.addAll(createObjectOrientedFlashcards());
        flashcards.addAll(createCollectionsFlashcards());
        flashcards.addAll(createExceptionHandlingFlashcards());
        flashcards.addAll(createConcurrencyFlashcards());
        flashcards.addAll(createAdvancedTopicsFlashcards());
        
        logger.info("Created {} default Java flashcards", flashcards.size());
        return flashcards;
    }

    /**
     * Java Basics category flashcards.
     */
    private static List<Flashcard> createJavaBasicsFlashcards() {
        List<Flashcard> cards = new ArrayList<>();
        
        cards.add(new Flashcard(
            "What is the difference between `==` and `.equals()` in Java?",
            "`==` compares object references (memory addresses), while `.equals()` compares object content. For primitives, `==` compares values directly.",
            "Java Basics",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What are the 8 primitive data types in Java?",
            "byte, short, int, long, float, double, boolean, char",
            "Java Basics",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "What is the difference between `String`, `StringBuilder`, and `StringBuffer`?",
            "`String` is immutable. `StringBuilder` is mutable and not thread-safe (faster). `StringBuffer` is mutable and thread-safe (slower due to synchronization).",
            "Java Basics",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What happens when you don't specify an access modifier in Java?",
            "The member has package-private (default) access, meaning it's accessible only within the same package.",
            "Java Basics",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "What is autoboxing and unboxing?",
            "Autoboxing is automatic conversion of primitives to their wrapper classes (int → Integer). Unboxing is the reverse (Integer → int). Java handles this automatically.",
            "Java Basics",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the difference between `final`, `finally`, and `finalize()`?",
            "`final` makes variables/methods/classes unchangeable. `finally` is a block that always executes after try-catch. `finalize()` is called by garbage collector before object destruction.",
            "Java Basics",
            DifficultyLevel.HARD
        ));
        
        return cards;
    }

    /**
     * Object-Oriented Programming category flashcards.
     */
    private static List<Flashcard> createObjectOrientedFlashcards() {
        List<Flashcard> cards = new ArrayList<>();
        
        cards.add(new Flashcard(
            "What are the four pillars of Object-Oriented Programming?",
            "1. Encapsulation (data hiding), 2. Inheritance (code reuse), 3. Polymorphism (many forms), 4. Abstraction (hiding complexity)",
            "Object-Oriented Programming",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "What is method overriding vs method overloading?",
            "Overriding: Same method signature in subclass (runtime polymorphism). Overloading: Same method name with different parameters in same class (compile-time polymorphism).",
            "Object-Oriented Programming",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the difference between abstract class and interface?",
            "Abstract class can have concrete methods and state; interface (pre-Java 8) only method declarations. A class can implement multiple interfaces but extend only one abstract class.",
            "Object-Oriented Programming",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the `super` keyword used for?",
            "`super` refers to the parent class. Used to call parent constructor (`super()`) or access parent methods/fields (`super.methodName()`).",
            "Object-Oriented Programming",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "Can you override static methods in Java?",
            "No, static methods cannot be overridden. They can be hidden by declaring a static method with the same signature in a subclass, but this is method hiding, not overriding.",
            "Object-Oriented Programming",
            DifficultyLevel.HARD
        ));
        
        return cards;
    }

    /**
     * Collections Framework category flashcards.
     */
    private static List<Flashcard> createCollectionsFlashcards() {
        List<Flashcard> cards = new ArrayList<>();
        
        cards.add(new Flashcard(
            "Which collection allows duplicate elements and maintains insertion order?",
            "ArrayList or LinkedList (both implement List interface which allows duplicates and maintains insertion order)",
            "Collections Framework",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "What is the difference between ArrayList and LinkedList?",
            "ArrayList uses dynamic array (fast random access, slow insertions/deletions). LinkedList uses doubly-linked list (slow random access, fast insertions/deletions).",
            "Collections Framework",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the difference between HashMap and TreeMap?",
            "HashMap: Hash table, O(1) average access, no ordering. TreeMap: Red-black tree, O(log n) access, maintains sorted order by keys.",
            "Collections Framework",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the difference between HashSet and LinkedHashSet?",
            "HashSet: No ordering, fastest. LinkedHashSet: Maintains insertion order using a linked list, slightly slower than HashSet.",
            "Collections Framework",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "What is the difference between Iterator and ListIterator?",
            "Iterator: Forward-only, works with all collections. ListIterator: Bidirectional, only works with Lists, allows modification during iteration.",
            "Collections Framework",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the difference between Comparable and Comparator?",
            "Comparable: Interface implemented by class itself (`compareTo()`), natural ordering. Comparator: External class for custom sorting (`compare()`), multiple sorting strategies.",
            "Collections Framework",
            DifficultyLevel.HARD
        ));
        
        return cards;
    }

    /**
     * Exception Handling category flashcards.
     */
    private static List<Flashcard> createExceptionHandlingFlashcards() {
        List<Flashcard> cards = new ArrayList<>();
        
        cards.add(new Flashcard(
            "What is the difference between checked and unchecked exceptions?",
            "Checked: Must be declared/handled (IOException, SQLException). Unchecked: Runtime exceptions, optional handling (RuntimeException, NullPointerException).",
            "Exception Handling",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the hierarchy of Exception classes?",
            "Throwable → Exception → RuntimeException (unchecked) and other Exception subclasses (checked). Throwable → Error (system errors, not typically caught).",
            "Exception Handling",
            DifficultyLevel.HARD
        ));
        
        cards.add(new Flashcard(
            "What is try-with-resources?",
            "A try statement that automatically closes resources (AutoCloseable/Closeable). Resources declared in try() parentheses are auto-closed after the block.",
            "Exception Handling",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "Can you have multiple catch blocks?",
            "Yes, multiple catch blocks can handle different exception types. More specific exceptions should be caught before general ones.",
            "Exception Handling",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "What happens if an exception occurs in finally block?",
            "Exception in finally block suppresses any exception from try/catch block. The finally block exception becomes the method's thrown exception.",
            "Exception Handling",
            DifficultyLevel.HARD
        ));
        
        return cards;
    }

    /**
     * Concurrency category flashcards.
     */
    private static List<Flashcard> createConcurrencyFlashcards() {
        List<Flashcard> cards = new ArrayList<>();
        
        cards.add(new Flashcard(
            "What is the difference between Thread and Runnable?",
            "Thread is a class (extends Thread). Runnable is an interface (implement Runnable). Runnable is preferred as Java supports single inheritance.",
            "Concurrency",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the difference between `synchronized` method and block?",
            "Synchronized method locks entire method on object/class. Synchronized block locks specific code section with chosen lock object, providing finer control.",
            "Concurrency",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the purpose of `volatile` keyword?",
            "`volatile` ensures variable visibility across threads and prevents instruction reordering. Changes are immediately visible to all threads.",
            "Concurrency",
            DifficultyLevel.HARD
        ));
        
        cards.add(new Flashcard(
            "What is a ThreadPool and why use ExecutorService?",
            "ThreadPool reuses threads for multiple tasks instead of creating new ones. ExecutorService manages thread lifecycle, provides better resource management and task scheduling.",
            "Concurrency",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is deadlock and how to prevent it?",
            "Deadlock: Two+ threads waiting for each other's resources forever. Prevention: Consistent lock ordering, timeout-based locking, avoid nested locks.",
            "Concurrency",
            DifficultyLevel.HARD
        ));
        
        return cards;
    }

    /**
     * Advanced Topics category flashcards.
     */
    private static List<Flashcard> createAdvancedTopicsFlashcards() {
        List<Flashcard> cards = new ArrayList<>();
        
        cards.add(new Flashcard(
            "What are Java Generics and why use them?",
            "Generics provide type safety at compile time. They eliminate casting, enable generic algorithms, and catch ClassCastException at compile time instead of runtime.",
            "Advanced Topics",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is type erasure in Java generics?",
            "Generic type information is removed at runtime for backward compatibility. Generic types become their raw types (List<String> becomes List).",
            "Advanced Topics",
            DifficultyLevel.HARD
        ));
        
        cards.add(new Flashcard(
            "What are lambda expressions and when were they introduced?",
            "Lambda expressions (Java 8) provide functional programming features. They're anonymous functions that can be passed as parameters: `(a, b) -> a + b`",
            "Advanced Topics",
            DifficultyLevel.MEDIUM
        ));
        
        cards.add(new Flashcard(
            "What is the difference between @Override and @Overload annotations?",
            "Only @Override exists in Java. It indicates method overriding and helps compiler check for errors. There's no @Overload annotation in Java.",
            "Advanced Topics",
            DifficultyLevel.EASY
        ));
        
        cards.add(new Flashcard(
            "What is reflection in Java?",
            "Reflection allows examining and modifying classes, methods, and fields at runtime. It enables accessing private members, dynamic instantiation, and frameworks like Spring.",
            "Advanced Topics",
            DifficultyLevel.HARD
        ));
        
        cards.add(new Flashcard(
            "What is the Stream API and what problem does it solve?",
            "Stream API (Java 8) provides functional-style operations on collections. It enables declarative data processing, lazy evaluation, and parallel processing.",
            "Advanced Topics",
            DifficultyLevel.MEDIUM
        ));
        
        return cards;
    }
}