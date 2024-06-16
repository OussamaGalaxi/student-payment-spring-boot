package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.Student;
import ma.dev7hd.studentspringngapp.enumirat.ProgramId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,String> {
    List<Student> findStudentByProgramId(ProgramId programId);
    Optional<Student> findStudentByCode(String code);
    void deleteByCode(String code);
}

