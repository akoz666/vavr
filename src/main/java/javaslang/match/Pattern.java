/**    / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.match;

import static javaslang.Requirements.requireNonNull;

import java.lang.invoke.MethodType;

import javaslang.Tuples;
import javaslang.Tuples.Tuple;
import javaslang.Tuples.Tuple1;
import javaslang.Tuples.Tuple2;
import javaslang.lambda.Lambdas;
import javaslang.option.Option;

/**
 * Represents a Pattern for pattern matching. In this context, Pattern matching is defined as
 * 
 * <ol>
 * <li>Perform check, if an object is applicable to the pattern.</li>
 * <li>If the pattern is applicable, decompose the object.</li>
 * <li>The pattern matches, if the prototype equals the decomposition result.</li>
 * <li>After a match, the typed object and the decomposition result of the object can be further processed.</li>
 * </ol>
 *
 * <strong>Note:</strong> Please not, that the equals check not necessary needs equal component types. I.e. the
 * prototype could contain components, which equal any object.
 * 
 * <strong>Note:</strong> The decomposition can return arbitrary, refined results depending or not depending on the
 * underlying object. With this property, code of further processing can be moved to the decomposition.
 *
 * @param <T> Object type to be decomposed.
 * @param <P> Type of prototype tuple containing components that will be compared with the decomposition result.
 * @param <R> Type of decomposition result.
 */
public class Pattern<T, P extends Tuple, R extends Tuple> {

	private final Class<T> decompositionType;
	private final Decomposition<T, R> decomposition;
	private final P prototype;

	/**
	 * Constructs a Pattern.
	 * 
	 * @param decompositionType
	 * @param decomposition
	 * @param prototype
	 */
	private Pattern(Class<T> decompositionType, Decomposition<T, R> decomposition, P prototype) {
		this.decompositionType = decompositionType;
		this.decomposition = decomposition;
		this.prototype = prototype;
	}

	/**
	 * Checks, if an object can be decomposed by this pattern.
	 * 
	 * @param obj An object to be tested.
	 * @return true, if the given object is not null and assignable to the decomposition type of this pattern.
	 */
	public boolean isApplicable(Object obj) {
		return obj != null && decompositionType.isAssignableFrom(obj.getClass());
	}

	/**
	 * Pattern matches the given object.
	 * 
	 * @param obj An object to be pattern matched.
	 * @return {@code Some(typedObject, decompositionOfTypedObject)}, if the prototype of this pattern equals the
	 *         decomposition result, otherwise {@code None}.
	 */
	@SuppressWarnings("unchecked")
	public Option<Tuple2<T, R>> apply(Object obj) {
		final T t = (T) obj;
		final R components = decomposition.unapply(t);
		if (prototype.equals(components)) {
			return Option.of(Tuples.of(t, components));
		} else {
			return Option.empty();
		}
	}

	/**
	 * Creates a Pattern for objects of type T. The Pattern matches a given object o, if {@code isApplicable(o)} returns
	 * true and {@code prototype.equals(decomposition.apply(o))} returns true.
	 * 
	 * @param <T> Object type to be decomposed.
	 * @param <P1> Type of component 1 of prototype.
	 * @param <R1> Type of component 1 of decomposition result.
	 * @param decomposition A Decomposition for objects of type T.
	 * @param prototype The prototype for comparision with the decomposition result.
	 * @return {@code Some(typedObject, decompositionOfTypedObject)} if the Pattern matches, otherwise {@code None}.
	 */
	public static <T, P1, R1> Pattern<T, Tuple1<P1>, Tuple1<R1>> of(Decomposition<T, Tuple1<R1>> decomposition,
			Tuple1<P1> prototype) {

		requireNonNull(decomposition, "decomposition is null");
		requireNonNull(prototype, "prototype is null");

		final MethodType methodType = Lambdas.getLambdaSignature(decomposition).get();
		@SuppressWarnings("unchecked")
		final Class<T> type = (Class<T>) methodType.parameterType(methodType.parameterCount() - 1);

		return new Pattern<>(type, decomposition, prototype);
	}

	public static <T, P1, P2, R1, R2> Pattern<T, Tuple2<P1, P2>, Tuple2<R1, R2>> of(
			Decomposition<T, Tuple2<R1, R2>> decomposition, Tuple2<P1, P2> prototype) {

		requireNonNull(decomposition, "decomposition is null");
		requireNonNull(prototype, "prototype is null");

		final MethodType methodType = Lambdas.getLambdaSignature(decomposition).get();
		@SuppressWarnings("unchecked")
		final Class<T> type = (Class<T>) methodType.parameterType(methodType.parameterCount() - 1);

		return new Pattern<>(type, decomposition, prototype);
	}

	// TODO: add Pattern.of(Decomposition, ...) factory methods for Tuple3, ... Tuple13.
}
