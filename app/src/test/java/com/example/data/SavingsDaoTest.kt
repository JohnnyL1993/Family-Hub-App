package com.example.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SavingsDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SavingsDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.savingsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `editing a goal updates its details and keeps its contributions`() = runBlocking {
        val memberId = dao.insertMember(FamilyMember(name = "Alex", avatarColorHex = "#4CAF50")).toInt()
        val goalId = dao.insertGoal(SavingsGoal(memberId = memberId, title = "Bike", targetAmount = 100.0)).toInt()
        dao.insertContribution(Contribution(goalId = goalId, amount = 40.0))
        val goal = dao.getAllGoals().first().single()

        dao.updateGoal(goal.copy(title = "Mountain Bike", targetAmount = 150.0, purchaseUrl = "https://example.com/bike"))

        val edited = dao.getAllGoals().first().single()
        assertEquals(goalId, edited.id)
        assertEquals("Mountain Bike", edited.title)
        assertEquals(150.0, edited.targetAmount, 0.0)
        assertEquals("https://example.com/bike", edited.purchaseUrl)
        assertEquals(goal.createdAt, edited.createdAt)

        val contributions = dao.getContributionsForGoal(goalId).first()
        assertEquals(1, contributions.size)
        assertEquals(40.0, contributions.single().amount, 0.0)
    }
}
