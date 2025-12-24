package org.myself.iamreviewing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.myself.iamreviewing.domain.dto.AttachmentDTO;
import org.myself.iamreviewing.domain.po.Attachment;
import org.myself.iamreviewing.domain.vo.AttachmentVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AttachmentService extends IService<Attachment> {
    List<AttachmentVO> getByPointId(Long id);

    boolean addAttachment(Long id, AttachmentDTO attachmentDTO);

    List<AttachmentVO> getAttachmentsByPointId(Long id);
}
