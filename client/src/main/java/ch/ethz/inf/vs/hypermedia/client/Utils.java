/*******************************************************************************
 * Copyright (c) 2016 Institute for Pervasive Computing, ETH Zurich.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Yassin N. Hassan - architect and implementation
 *******************************************************************************/
package ch.ethz.inf.vs.hypermedia.client;

import com.google.common.collect.FluentIterable;
import org.apache.http.client.utils.URIUtils;
import org.eclipse.californium.core.WebLink;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import com.google.common.base.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ynh on 30/09/15.
 */
public class Utils {

	public static String resolve(String base, String... relativePaths) throws URISyntaxException {
		URI uri = new URI(base);
		for (String relativePath : relativePaths) {
			if (relativePath != null) {
				uri = URIUtils.resolve(uri, relativePath);
			}
		}
		return uri.toString();
	}

	public static <T extends Enum<T>> T selectFromEnum(Class<T> enumType, Scanner scanner, String prompt) {
		T[] consts = enumType.getEnumConstants();
		String options = Stream.of(consts).map(x -> x.ordinal() + ": " + x).collect(Collectors.joining(", "));
		int index = -1;
		do {
			System.out.printf("%s (%s): %n", prompt, options);
			String val = scanner.next().trim();
			try {
				index = Integer.parseInt(val);
			} catch (NumberFormatException e) {

			}
		} while (!(index >= 0 && index < consts.length));
		return consts[index];
	}

	public static <V> int getContentType(Class<V> type) {
		return getMediaTypeAnnotation(type).contentType();
	}

	public static <V> String getMediaType(Class<V> type) {
		return getMediaTypeAnnotation(type).mediaType();
	}

	public static <V> MediaType getMediaTypeAnnotation(Class<V> type) {
		return type.getAnnotation(MediaType.class);
	}

	public static <V extends Future> V or(Supplier<V> s, V... options) {
		return or(s, Arrays.asList(options));
	}

	public static <V extends Future> V or(Supplier<V> s, Iterable<V> options) {
		V item = s.get();
		item.setPreProcess(() -> {
			for (V option : options) {
				try {
					item.addDependency(option);
					option.get();
					item.link(option);
					return;
				} catch (Exception ex) {

				}
			}
			item.setException(new RuntimeException("All options Failed"));
		});
		return item;
	}

	public static <V extends Future> V find(Supplier<V> s, Predicate<V> p, Iterable<V> options) {
		return or(s, FluentIterable.from(options).filter(p));
	}

	/**
	 * http://www.artima.com/weblogs/viewpost.jsp?thread=208860 Get the actual
	 * type arguments a child class has used to extend a generic base class.
	 *
	 * @param baseClass
	 *            the base class
	 * @param childClass
	 *            the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		while (!getClass(type).equals(baseClass)) {
			if (type instanceof Class) {
				// there is no useful information for us in raw types, so just
				// keep going.
				type = ((Class) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!rawType.equals(baseClass)) {
					type = rawType.getGenericSuperclass();
				}
			}
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class) {
			actualTypeArguments = ((Class) type).getTypeParameters();
		} else {
			actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
		}
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType : actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType)) {
				baseType = resolvedTypes.get(baseType);
			}
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}

	public static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else {
			return null;
		}
	}

	public static String getWebLinkAttribute(WebLink x, String attr) {
		List<String> attrs = x.getAttributes().getAttributeValues(attr);
		return attrs.size() > 0 ? attrs.get(0) : null;
	}
}
