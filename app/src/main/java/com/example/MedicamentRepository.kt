package com.mediapp.interactions.data.repository

import com.mediapp.interactions.data.local.MedicamentDao
import com.mediapp.interactions.data.model.Gravite
import com.mediapp.interactions.data.model.InteractionResult
import com.mediapp.interactions.data.model.Medicament
import com.mediapp.interactions.data.remote.OpenFdaApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicamentRepository @Inject constructor(
    private val medicamentDao: MedicamentDao,
    private val openFdaApi: OpenFdaApi
) {
    fun getAllMedicaments(): Flow<List<Medicament>> = medicamentDao.getAll()

    suspend fun addMedicament(medicament: Medicament): Long {
        return medicamentDao.insert(medicament)
    }

    suspend fun deleteMedicament(medicament: Medicament) {
        medicamentDao.delete(medicament)
    }

    suspend fun deleteMedicamentById(id: Long) {
        medicamentDao.deleteById(id)
    }

    suspend fun checkInteractions(medicaments: List<Medicament>): List<InteractionResult> {
        val results = mutableListOf<InteractionResult>()
        if (medicaments.size < 2) return results

        for (i in medicaments.indices) {
            for (j in i + 1 until medicaments.size) {
                val m1 = medicaments[i]
                val m2 = medicaments[j]
                val interaction = searchInteraction(m1, m2)
                if (interaction != null) {
                    results.add(interaction)
                }
            }
        }
        return results
    }

    private suspend fun searchInteraction(m1: Medicament, m2: Medicament): InteractionResult? {
        return try {
            val response = openFdaApi.searchDrug("openfda.substance_name:"${m1.principeActif}"")
            if (response.isSuccessful) {
                val body = response.body()
                val interactions = body?.results?.firstOrNull()?.drug_interactions
                if (interactions != null) {
                    val found = interactions.any { it.contains(m2.principeActif, ignoreCase = true) }
                    if (found) {
                        InteractionResult(
                            medicament1 = m1.nom,
                            medicament2 = m2.nom,
                            description = "Interaction potentielle détectée entre ${m1.nom} et ${m2.nom}",
                            gravite = Gravite.MODEREE
                        )
                    } else null
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
