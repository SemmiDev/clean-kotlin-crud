package com.sammidev

import org.hibernate.annotations.DynamicUpdate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@SpringBootApplication
class CrudKotlinApplication

fun main(args: Array<String>) {
	runApplication<CrudKotlinApplication>(*args)
}

@Entity
@Table(name = "mahasiswa")
@DynamicUpdate
class Mahasiswa {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long = 0

	@get: NotBlank
	var nama: String = ""

	@get: NotBlank
	var nim: String = ""

	@get: NotBlank
	var prodi: String = ""
}

@Repository
interface MahasiswaRepository : JpaRepository<Mahasiswa, Long>

data class MahasiswaVO (
		var id: Long = 0,
		var nama: String = "",
		var nim: String = "",
		var prodi: String = ""
)

fun Mahasiswa.toVo(): MahasiswaVO {
	return MahasiswaVO(
			id = id,
			nama = nama,
			nim = nim,
			prodi = prodi
	)
}

fun MahasiswaVO.toModel() : Mahasiswa {
	return Mahasiswa().apply {
		id = this@toModel.id
		nama = this@toModel.nama
		nim = this@toModel.nim
		prodi = this@toModel.prodi
	}
}

@Service
class MahasiswaService {
	@Autowired
	lateinit var mahasiswaRepository: MahasiswaRepository

	fun saveData(mahasiswa: Mahasiswa): MahasiswaVO {
		return mahasiswaRepository.save(mahasiswa).toVo()
	}

	fun findById(id: Long): Optional<MahasiswaVO> {
		return mahasiswaRepository.findById(id).map { it.toVo() }
	}

	fun getAll() : List<MahasiswaVO> {
		return mahasiswaRepository.findAll().map {
			it.toVo()
		}
	}

	fun editData(mahasiswa: Mahasiswa) : MahasiswaVO {
		val model = mahasiswaRepository.getOne(mahasiswa.id)
		model.apply {
			nama = mahasiswa.nama
			nim = mahasiswa.nim
			prodi = mahasiswa.prodi
		}
		return mahasiswaRepository.saveAndFlush(model).toVo()
	}

	fun deleteMahasiwa(id: Long): String {
		val model: Mahasiswa = mahasiswaRepository.getOne(id)
		return if (model == null) {
			"DATA NOT FOUND"
		}else {
			mahasiswaRepository.delete(model)
			"OK"
		}
	}
}


@RestController
@RequestMapping("/api/mahasiswa")
class MahasiswaController {
	@Autowired
	lateinit var mahasiswaService: MahasiswaService

	@GetMapping("/get-all")
	fun getMahasiswa(): List<MahasiswaVO> {
		return mahasiswaService.getAll();
	}

	@GetMapping("/getone/{id}")
	fun getMahasiswa(@PathVariable(value = "id") id: Long): Optional<MahasiswaVO> {
		return mahasiswaService.findById(id);
	}

	@PostMapping("/add")
	fun addMahasiswa(@Valid @RequestBody mahasiswaVO: MahasiswaVO): MahasiswaVO {
		return mahasiswaService.saveData(mahasiswaVO.toModel())
	}

	@PutMapping("/edit")
	fun updateMahasiswaById(@RequestBody mahasiswa: Mahasiswa): MahasiswaVO {
		return mahasiswaService.editData(mahasiswa)
	}

	@DeleteMapping("hapus/{id}")
	fun deleteMahasiswaById(@PathVariable id: Long): String {
		return mahasiswaService.deleteMahasiwa(id)
	}
}