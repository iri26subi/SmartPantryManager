package com.smartpantry.manager.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UnitConverterTest {

    private static final double DELTA = 1e-9;

    @Test
    public void normalisesAliases() {
        assertEquals("g", UnitConverter.normalizeUnit("Grams"));
        assertEquals("kg", UnitConverter.normalizeUnit("kilo"));
        assertEquals("L", UnitConverter.normalizeUnit("litres"));
        assertEquals("L", UnitConverter.normalizeUnit("l"));
        assertEquals("ml", UnitConverter.normalizeUnit("ML"));
        assertEquals("tbsp", UnitConverter.normalizeUnit("Tablespoons"));
        assertEquals("tsp", UnitConverter.normalizeUnit("tsp."));
        assertEquals("pcs", UnitConverter.normalizeUnit("pieces"));
        assertEquals("cup", UnitConverter.normalizeUnit("cups"));
        assertNull(UnitConverter.normalizeUnit("handful"));
        assertNull(UnitConverter.normalizeUnit(null));
    }

    @Test
    public void everyOfferedUnitIsSupported() {
        for (String unit : UnitConverter.SUPPORTED_UNITS) {
            assertTrue(unit, UnitConverter.isSupported(unit));
            assertEquals(unit, UnitConverter.normalizeUnit(unit));
        }
    }

    @Test
    public void convertsWithinTheSameDimension() {
        assertEquals(1500, UnitConverter.toBase(1.5, "kg"), DELTA);
        assertEquals(250, UnitConverter.toBase(1, "cup"), DELTA);
        assertEquals(30, UnitConverter.toBase(2, "tbsp"), DELTA);
        assertEquals(5, UnitConverter.toBase(1, "tsp"), DELTA);
        assertEquals(0.5, UnitConverter.convert(500, "ml", "L"), DELTA);
        assertEquals(3, UnitConverter.convert(1, "tbsp", "tsp"), DELTA);
        assertEquals(4, UnitConverter.convert(1, "L", "cup"), DELTA);
    }

    @Test
    public void massAndVolumeAreNotCompatible() {
        assertFalse(UnitConverter.areCompatible("g", "ml"));
        assertFalse(UnitConverter.areCompatible("kg", "cup"));
        assertFalse(UnitConverter.areCompatible("pcs", "g"));
        assertFalse(UnitConverter.areCompatible("g", "handful"));
        assertTrue(UnitConverter.areCompatible("g", "kg"));
        assertTrue(UnitConverter.areCompatible("tsp", "L"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertingIncompatibleUnitsThrows() {
        UnitConverter.convert(1, "g", "ml");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownUnitThrows() {
        UnitConverter.toBase(1, "pinch");
    }
}
