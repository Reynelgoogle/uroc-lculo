package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGleasonToIsupMapping() {
    // Grade Group 1
    assertEquals(1, getIsupGrade(3, 3))

    // Grade Group 2
    assertEquals(2, getIsupGrade(3, 4))

    // Grade Group 3
    assertEquals(3, getIsupGrade(4, 3))

    // Grade Group 4
    assertEquals(4, getIsupGrade(4, 4))
    assertEquals(4, getIsupGrade(3, 5))
    assertEquals(4, getIsupGrade(5, 3))

    // Grade Group 5
    assertEquals(5, getIsupGrade(5, 5))
    assertEquals(5, getIsupGrade(4, 5))
    assertEquals(5, getIsupGrade(5, 4))
  }

  @Test
  fun testRenalScoreAndComplexity() {
    // Scenario 1: Minimum score (low complexity)
    val score1 = calculateRenalScore(1, 1, 1, 1)
    assertEquals(4, score1)
    assertEquals("Baja Complejidad Quirúrgica", getRenalComplexity(score1))

    // Scenario 2: Moderate complexity
    val score2 = calculateRenalScore(2, 2, 2, 2)
    assertEquals(8, score2)
    assertEquals("Complejidad Quirúrgica Moderada", getRenalComplexity(score2))

    // Scenario 3: Maximum score (high complexity)
    val score3 = calculateRenalScore(3, 3, 3, 3)
    assertEquals(12, score3)
    assertEquals("Alta Complejidad Quirúrgica", getRenalComplexity(score3))
  }

  private fun calculateRenalScore(r: Int, e: Int, n: Int, l: Int): Int {
    return r + e + n + l
  }

  private fun getRenalComplexity(totalScore: Int): String {
    return when {
        totalScore in 4..6 -> "Baja Complejidad Quirúrgica"
        totalScore in 7..9 -> "Complejidad Quirúrgica Moderada"
        else -> "Alta Complejidad Quirúrgica"
    }
  }

  private fun getIsupGrade(primaryPattern: Int, secondaryPattern: Int): Int {
    return when {
        primaryPattern == 3 && secondaryPattern == 3 -> 1
        primaryPattern == 3 && secondaryPattern == 4 -> 2
        primaryPattern == 4 && secondaryPattern == 3 -> 3
        (primaryPattern == 4 && secondaryPattern == 4) || (primaryPattern == 3 && secondaryPattern == 5) || (primaryPattern == 5 && secondaryPattern == 3) -> 4
        (primaryPattern == 5 && secondaryPattern == 5) || (primaryPattern == 4 && secondaryPattern == 5) || (primaryPattern == 5 && secondaryPattern == 4) -> 5
        else -> 1
    }
  }
}
