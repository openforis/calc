package org.openforis.calc.metadata;

import javax.persistence.DiscriminatorValue;

/**
 * A special type of categorical variable which may take one of three values; TRUE, FALSE or NA.
 * 
 * TODO: remove if not needed; create TRUE, FALSE and NA const Categorys if needed
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@DiscriminatorValue("B")
public class BinaryVariable extends Variable {
}