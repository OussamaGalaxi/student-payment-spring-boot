package ma.dev7hd.studentspringngapp.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.PaymentDTO;
import ma.dev7hd.studentspringngapp.entities.Payment;
import ma.dev7hd.studentspringngapp.entities.Student;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.repositories.PaymentRepository;
import ma.dev7hd.studentspringngapp.repositories.StudentRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class PaymentService {
    private PaymentRepository paymentRepository;
    private StudentRepository studentRepository;

    public ResponseEntity<Payment> newPayment(PaymentDTO paymentDTO,
                                              MultipartFile file) throws IOException {
        if (!file.getContentType().equals(MediaType.APPLICATION_PDF_VALUE)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Student> optionalStudent = studentRepository.findStudentByCode(paymentDTO.getStudentCode());
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            Path folderPath = Paths.get(System.getProperty("user.home"),"data","payments");
            if(!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            String fileName = UUID.randomUUID().toString();
            Path filePath = Paths.get(folderPath.toString(),fileName+".pdf");
            Files.copy(file.getInputStream(),filePath);
            Payment payment = Payment.builder()
                    .amount(paymentDTO.getAmount())
                    .student(student)
                    .type(paymentDTO.getPaymentType())
                    .date(paymentDTO.getDate())
                    .status(PaymentStatus.CREATED)
                    .receipt(filePath.toUri().toString())
                    .build();
            paymentRepository.save(payment);
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Payment> paymentStatusUpdate(String id, PaymentStatus status) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            payment = paymentRepository.save(payment);
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public byte[] getPaymentFile(String paymentId) throws IOException {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            return Files.readAllBytes(Path.of(URI.create(optionalPayment.get().getReceipt())));
        }
        return null;
    }

    public void deleteStudentPayments(String id){
        paymentRepository.deleteAllByStudentId(id);
    }

}
