/*
 * Copyright 2010 Pablo Arrighi, Alex Concha, Miguel Lezama for version 1.
 * Copyright 2013 Pablo Arrighi, Miguel Lezama, Kevin Mazet for version 2.    
 *
 * This file is part of GOOL.
 *
 * GOOL is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, version 3.
 *
 * GOOL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License along with GOOL,
 * in the file COPYING.txt.  If not, see <http://www.gnu.org/licenses/>.
 */

package gool.ast.type;

import gool.ast.core.ClassDef;
import gool.generator.GoolGeneratorController;

/**
 * This is the basic type for map in the intermediate language.
 */
public class TypeMap extends ReferenceType {

	/**
	 * The class where the map was defined.
	 */
	private ClassDef classDef;

	/**
	 * The empty constructor of a "type map" representation.
	 */
	public TypeMap() {
	}

	/**
	 * The constructor of a "type map" representation.
	 * @param keyType
	 * 		: The type of the keys used by the map.
	 * @param elementType
	 * 		: The type of the elements used by the map.
	 */
	protected TypeMap(IType keyType, IType elementType) {
		this();
		addArgument(keyType);
		addArgument(elementType);
	}

	/**
	 * Gets the type of the elements used by the map.
	 * @return
	 * 		The type of the elements used by the map.
	 */
	public IType getElementType() {
		if (getTypeArguments().size() > 1) {
			return getTypeArguments().get(1);
		}
		return TypeObject.INSTANCE;
	}

	/**
	 * Gets the type of the keys used by the map.
	 * @return
	 * 		The type of the keys used by the map.
	 */
	public IType getKeyType() {
		if (getTypeArguments().size() > 1) {
			return getTypeArguments().get(0);
		}
		return TypeObject.INSTANCE;
	}

	@Override
	public String callGetCode() {
		return GoolGeneratorController.generator().getCode(this);
	}
	
	/**
	 * Gets the class' definition where the map was defined.
	 * @return
	 * 		The class' definition where the map was defined.
	 */
	public ClassDef getClassDef() {
		return classDef;
	}

	/**
	 * Sets the class' definition where the map was defined.
	 * @param classDef
	 * 		: The new class' definition where the map was defined.
	 */
	public void setClassDef(ClassDef classDef) {
		this.classDef = classDef;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TypeMap
				&& getName().equals(((IType) obj).getName());
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public String getName() {
		return this.callGetCode();
	}

}
