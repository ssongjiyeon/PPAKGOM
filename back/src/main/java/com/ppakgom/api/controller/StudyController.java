package com.ppakgom.api.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

import com.ppakgom.common.auth.SsafyUserDetails;
import com.ppakgom.common.model.response.BaseResponseBody;
import com.ppakgom.db.entity.Interest;
import com.ppakgom.db.entity.Study;
import com.ppakgom.db.entity.StudyApply;
import com.ppakgom.db.entity.StudyPlan;
import com.ppakgom.db.entity.StudyRate;
import com.ppakgom.db.entity.User;
import com.ppakgom.db.entity.UserInterest;
import com.ppakgom.db.entity.UserStudy;
import com.ppakgom.db.repository.StudyInterestRepository;
import com.ppakgom.db.repository.UserStudyRepository;
import com.ppakgom.api.response.AttendGetRes;
import com.ppakgom.api.response.InviteGetResByStudy;
import com.ppakgom.api.response.InviteResByStudy;
import com.ppakgom.api.response.SearchMember;
import com.ppakgom.api.response.RateRes;
import com.ppakgom.api.response.StudyCreatePostRes;
import com.ppakgom.api.response.StudyDetailInfo;
import com.ppakgom.api.response.StudyJoinApplyListRes;
import com.ppakgom.api.response.StudyMemberInfoRes;
import com.ppakgom.api.response.StudyRes;
import com.ppakgom.api.response.StudyScheduleMonthRes;
import com.ppakgom.api.response.StudySearchGetRes;
import com.ppakgom.api.response.StudyTestInfoRes;
import com.ppakgom.api.response.StudyTestListRes;
import com.ppakgom.api.response.StudyTestScoreRes;
import com.ppakgom.api.service.InterestService;
import com.ppakgom.api.service.StudyApplyService;
import com.ppakgom.api.service.StudyRateService;

import com.ppakgom.api.response.StudyTestScoreTotalRes;

import com.ppakgom.api.service.JoinService;
import com.ppakgom.api.service.StudyService;
import com.ppakgom.api.service.StudyTestService;
import com.ppakgom.api.service.UserService;
import com.ppakgom.api.service.UserStudyService;
import com.ppakgom.api.service.UserInterestService;
import com.ppakgom.api.request.StudyCreatePostReq;

import com.ppakgom.api.request.StudyInvitePostReq;
import com.ppakgom.api.request.StudyRatePostReq;
import com.ppakgom.api.request.WorkbookCreatePostReq;

import com.ppakgom.api.request.StudyScheduleReq;

/**
 * 스터디 CRUD 관련 API 요청을 처리하는 컨트롤러
 */

@Api(value = "스터디 API", tags = { "Study" })
@RestController
@RequestMapping("api/v1/study")
public class StudyController {

	@Autowired
	UserService userService;

	@Autowired
	StudyService studyService;

	@Autowired
	StudyApplyService studyApplyService;

	@Autowired
	StudyInterestRepository studyInterestRepository;

	@Autowired
	UserStudyRepository userStudyRepository;

	@Autowired
	JoinService joinService;

	@Autowired
	UserInterestService userInterestService;

	@Autowired
	InterestService interestService;

	@Autowired
	UserStudyService userStudyService;

	private final StudyRes STUDY_RES = new StudyRes();

	@Autowired
	StudyRateService studyRateService;

	@Autowired
	StudyTestService studyTestService;

	/* 스터디 생성 */
	@PostMapping("/")
	@ApiOperation(value = "스터디 생성", notes = "스터디 명, 마감인원 등을 받으면 스터디를 생성합니다.", consumes = "multipart/form-data", produces = "multipart/form-data")
	public ResponseEntity<?> createStudy(@ApiParam(value = "로그인 정보", required = true) StudyCreatePostReq studyInfo,
			@RequestPart(value = "study_thumbnail", required = false) MultipartFile studyThumbnail,
			@ApiIgnore Authentication authentication) {

		Study study = null;

		SsafyUserDetails userDetails = (SsafyUserDetails) authentication.getDetails();
		String userId = userDetails.getUsername();
		User user = userService.getUserByUserId(userId);

		try {

			study = studyService.createStudy(studyInfo, user, studyThumbnail);

		} catch (ParseException e) {
			System.err.println("날짜 파싱 에러");
			e.printStackTrace();
			BaseResponseBody res = new BaseResponseBody(500, "서버 에러");
			return ResponseEntity.status(500).body(res);
		} catch (Exception e) {
			System.err.println("파일 저장 에러");
			e.printStackTrace();
			BaseResponseBody res = new BaseResponseBody(500, "서버 에러");
			return ResponseEntity.status(500).body(res);

		}
//		성공 응답 -> 아뒤
		StudyCreatePostRes res = new StudyCreatePostRes(study.getId());
		return ResponseEntity.ok(res);
	}

//	/* 스터디 검색 */
	@GetMapping("/")
	@ApiOperation(value = "스터디 검색", notes = "전체 스터디 목록 검색")
	public ResponseEntity<StudySearchGetRes> searchStudyById(@RequestParam(required = false) Long studyId,
			@RequestParam(required = false) String name, @RequestParam(required = false) String interest,
			@ApiIgnore Authentication authentication) {

		StudySearchGetRes res = new StudySearchGetRes();
		res.setStudyResult(new ArrayList<>());
		List<Study> resultSet = new ArrayList<>();
		Optional<Study> study;
		try {

			try {

//		스터디 전체 검색
				if (studyId == null && name == null && interest == null)
					resultSet = studyService.getAllStudy();

//		아이디로 검색
				if (studyId != null) {
					study = studyService.getStudyById(studyId);
					resultSet.add(study.orElse(null));
				}

//		스터디명으로 검색
				if (name != null) {
					resultSet = studyService.getStudyByName(name);
				}

//		관심사로 검색
				if (interest != null) {
					resultSet = studyService.getStudyByInterest(interest);
				}

				List<Study> userJoinStudy = new ArrayList<Study>();
				List<Study> userLikedStudy = null;
				
				if (authentication == null) {
					System.out.println("로그인된 사용자 없음");
				} else {
					SsafyUserDetails userDetails = (SsafyUserDetails) authentication.getDetails();
					String userId = userDetails.getUsername();
					User curUser = userService.getUserByUserId(userId);
					System.out.println("로그인한 사용자 " + curUser);
					userJoinStudy = studyService.getUserJoinStudy(curUser);
					userLikedStudy = studyService.getUserLikeStudy(curUser);

				}

//			오늘 날짜 받기
				Date today = new Date();
				System.out.println("오늘 날짜 " + today);
				/* 검색 결과 삽입 */
				for (Study s : resultSet) {
//				마감날짜 지났으면 pass
					Date strDate = s.getDeadline();
					if (strDate.before(today)) {
						continue;
					}
					StudyRes sr = STUDY_RES.of(s, studyInterestRepository, userStudyRepository, userJoinStudy, userLikedStudy);
					res.getStudyResult().add(sr);
				}
			} catch (Exception e) {
				System.out.println("검색 결과 없음!");
			}
		} catch (Exception e) {
			System.out.println("에러");
		}
		return ResponseEntity.ok(res);

	}

	@GetMapping("/{studyId}/joinlist")
	@ApiOperation(value = "스터디 내에서 가입 요청 리스트 가져오기", notes = "스터디 내에서 가입 요청 리스트 가져오기")
	public ResponseEntity<List<StudyJoinApplyListRes>> studyJoinApplyListRes(@PathVariable Long studyId) {

		List<StudyJoinApplyListRes> res = joinService.getStudyJoinApplyList(studyId);
		return ResponseEntity.status(200).body(res);
	}

	@GetMapping("/{studyId}/schedule")
	@ApiOperation(value = "스터디 방 스케줄 정보 가져오기", notes = "스터디 방 스케줄 정보 가져오기")
	public ResponseEntity<List<StudyScheduleMonthRes>> studyScheduleMonth(@PathVariable Long studyId,
			@RequestParam(required = true) int month) {

		// 스터디 방 스케쥴 정보 가져오기
		List<StudyScheduleMonthRes> res = studyService.getStudyScheduleMonth(studyId, month);
		return ResponseEntity.status(200).body(res);
	}

	/* 사용자 관심 스터디 불러오기 */
	@GetMapping("/interest/{userId}")
	@ApiOperation(value = "관심사 기반 스터디 검색", notes = "사용자 관심사 기반 스터디 검색")
	public ResponseEntity<StudySearchGetRes> searchStudyByUserInterest(
			@PathVariable(value = "userId") @ApiParam(value = "사용자 ID", required = true) Long userId) {
		StudySearchGetRes res = new StudySearchGetRes();
		res.setStudyResult(new ArrayList<>());
		// 사용자의 관심사들에 매칭된 스터디가 겹칠 경우.
		// 예: 사용자 관심사: 면접, 대기업이고 한 스터디 관심사도 면접, 대기업 인 경우 해당 스터디가 두 번삽입되는 문제 방지.
		HashSet<Study> tmp = new HashSet<>();

//		1. 사용자 관심사 불러오기.
		User user = userService.getUserById(userId);
		List<UserInterest> userInterest = userInterestService.getInterestByUser(user);
//		2. 관심사에 맞는 스터디 불러오기
		List<Study> resultSet;
//		관심사 있는지부터 체크
		if (userInterest != null) {
			for (UserInterest ui : userInterest) {
//				최대 3개만 저장스
//				System.out.println("사용자 "+ui.getUser()+" "+"관심사: "+ui.getInterest());
				resultSet = studyService.getStudyByInterest(ui.getInterest().getName());
				for (Study s : resultSet) {
					tmp.add(s);
//					if (tmp.size() == 3)
//						break;
				}
//				if (tmp.size() == 3)
//					break;
			}
		}
		List<Study> userStudy = studyService.getUserJoinStudy(user);
		List<Study> userLikedStudy = studyService.getUserLikeStudy(user);
		
		/* 검색 결과 삽입 */
		for (Study s : tmp) {
//			내가 가입한 스터디 제외시키기
			if(userStudy.contains(s))
				continue;
			StudyRes sr = STUDY_RES.of(s, studyInterestRepository, userStudyRepository, userStudy, userLikedStudy);
			res.getStudyResult().add(sr);
		}

		return ResponseEntity.ok(res);

	}

	/* 스터디 상세 정보 불러오기 */
	@GetMapping("/{studyId}/detail")
	@ApiOperation(value = "스터디 상세 정보 조회", notes = "방장 id를 포함한 상세 정보 조회")
	public ResponseEntity<StudySearchGetRes> getStudyDetail(
			@PathVariable(value = "studyId") @ApiParam(value = "스터디 ID", required = true) Long studyId,
			@ApiIgnore Authentication authentication) {

		StudySearchGetRes res = new StudySearchGetRes();
		res.setStudyResult(new ArrayList<>()); // 배열로 안줘도 되는데 내가 배열로 준다고 해버려서 ... 추후 논의쓰

		Optional<Study> study = studyService.getStudyById(studyId);
//		입장 버튼 추가용
		SsafyUserDetails userDetails = (SsafyUserDetails) authentication.getDetails();
		String userId = userDetails.getUsername();
		User user = userService.getUserByUserId(userId);

		List<Study> userStudy = studyService.getUserJoinStudy(user);
		List<Study> userLikedStudy = studyService.getUserLikeStudy(user);

		
		if (study.isPresent()) {
			res.getStudyResult()
					.add(new StudyRes().of(study.get(), studyInterestRepository, userStudyRepository, userStudy,userLikedStudy));
		}
		return ResponseEntity.ok(res);

	}

	/* 평가 점수 입력 */
	@PostMapping("/rating/{userId}")
	@ApiOperation(value = "스터디원 평가하기", notes = "스터디원 점수를 5점 만점에 정수로 평가하기")
	public ResponseEntity<BaseResponseBody> rateStudyMember(
			@PathVariable(value = "userId") @ApiParam(value = "사용자 ID", required = true) Long userId,
			@ApiParam(value = "평가 내용", required = true) StudyRatePostReq rateInfo) {

//		유저 ID, 스터디 ID, 스터디 멤버 ID
		try {
//			유저 ID로 평가자 객체 찾기
			User user = userService.getUserById(userId);
			studyService.rateStudy(user, rateInfo);
			return ResponseEntity.status(200).body(new BaseResponseBody(200, "평가 완료"));
		}
//		존재 하지 않는 스터디 || 존재하지 않는 유저 || 존재하지 않는 멤버
		catch (Exception e) {
			return ResponseEntity.status(400).body(new BaseResponseBody(400, "다시 시도해 주세요."));
		}
	}

	/* 평가 목록 불러오기 */
	// 내가 가입한 스터디 중 내가 평가 한 or 평가 해야 하는 스터디 목록이 쫘르르 나온다.
	@GetMapping("/rating/{userId}")
	@ApiOperation(value = "평가할 스터디원 목록.", notes = "평가 했던 or 평가 해야할 스터디원 목록 불러오기")
	public ResponseEntity<?> getRateList(
			@PathVariable(value = "userId") @ApiParam(value = "사용자 ID", required = true) Long userId) {
		List<StudyRate> rateList = studyRateService.getRateListByUserId(userId);
		List<RateRes> rateRes = new ArrayList<>();

		for (StudyRate sr : rateList) {
			rateRes.add(new RateRes(sr));
		}

		return ResponseEntity.ok(rateRes);
	}

	/* 스터디 초대하기 */
//	스터디에 초대한다(방장만 가능)
	@PostMapping("/{studyId}/member")
	@ApiOperation(value = "스터디에 초대하기", notes = "방장이 스터디에 회원을 초대한다.")
	public ResponseEntity<BaseResponseBody> inviteMember(@PathVariable(value = "studyId") Long studyId,
			@ApiParam(value = "스터디 초대 내용", required = true) StudyInvitePostReq req) {
		try {
			// receiver_id로 회원 찾고
			User receiver = userService.getUserById(req.getReceiver_id());
			// study_id로 스터디 찾고
			Study study = studyService.getStudyById(studyId).get();

//			이미 회원이거나
			Optional<UserStudy> us = userStudyService.findUserStudyByUserIdAndStudyId(receiver.getId(), study.getId());
			if(us.isPresent())
				return ResponseEntity.status(200).body(new BaseResponseBody(200, "이미 가입한 회원은 초대할 수 없습니다."));

//			가입 요청 받은 유저면 초대 불가. -> study_apply 에서 sender_id, receiver_id, isJoin(true), study_id로 확인
			Optional<StudyApply> studyApply = studyApplyService.findStudyApplyBySenderIdAndReceiverIdAndIsJoinAndStudyId(
																				req.getReceiver_id(),study.getUser().getId(),true,study.getId());
			if(studyApply.isPresent())
				return ResponseEntity.status(200).body(new BaseResponseBody(200, "이미 가입요청한 회원입니다."));
			
			
			if (study.getTemperature() > receiver.getTemperature()) {
				return ResponseEntity.status(400).body(new BaseResponseBody(400, "열정도가 낮은 회원은 초대할 수 없습니다."));
			}

			// owner_id 가 sender_id.
			User sender = study.getUser();
			// is_join 가지고
			// state는 2
//			중복 방지 코드 추가
			Optional<StudyApply> sa = studyApplyService.getInviteListByStudyAndIsJoinAndReceiverId(studyId, false,req.getReceiver_id());
			
			if(!sa.isPresent())
				studyApplyService.inviteStudy(sender, study, receiver, req.is_join());

			return ResponseEntity.ok(new BaseResponseBody(200, "성공"));
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(400).body(new BaseResponseBody(400, "실패"));
		}
	}

	/* 초대한 회원 리스트 */
	@GetMapping("/{studyId}/invitelist")
	@ApiOperation(value = "스터디에 초대한 회원 리스트 ", notes = "방장이 스터디에 초대한 회원 리스트")
	public ResponseEntity<?> getInviteListOfStudy(@PathVariable(value = "studyId") Long studyId) {

//		study로 질의
		List<StudyApply> temp = studyApplyService.getInviteListByStudyAndIsJoin(studyId, false);

		InviteGetResByStudy res = new InviteGetResByStudy();
		for (StudyApply sa : temp) {
			InviteResByStudy resByStudy = new InviteResByStudy();
			res.getInviteResult().add(resByStudy.of(sa));
		}

		return ResponseEntity.ok(res);
	}

	/* 검색한 해시태그로 회원 리스트 불러오기 */
	@GetMapping("/{studyId}/member/{interest}")
	@ApiOperation(value = "관심사를 가진 회원 리스트", notes = "해당 관심사를 가진 회원 리스트")
	public ResponseEntity<?> getMemberByInterest(@PathVariable(value = "studyId") Long studyId,
			@PathVariable(value = "interest") String interest) {

		List<SearchMember> res = new ArrayList<>();
		try {

//		1. 해당 단어가 포함된 관심사를 불러온다.
			List<Interest> interestThings = interestService.getInterestByName(interest);

			HashSet<User> interestedUsers = new HashSet<>();

//		2. 회원 - 관심사 테이블에서 그에 맞는 회원을 리스트로 가져온다.
			for (Interest i : interestThings) {
				List<UserInterest> userInterest = userInterestService.findByInterestId(i.getId());
				for (UserInterest ui : userInterest) {
					interestedUsers.add(ui.getUser());
				}
			}

//		3. 회원이 가입한 스터디를 회원 - 스터디 테이블에서 가져오고, 그에 맞게 응답 객체를 생성하고 삽입한다.
			for (User u : interestedUsers) {
//				List<UserStudy> studyList = userStudyService.findUserStudyByUserId(u.getId());
				res.add(new SearchMember(u, null));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("검색결과 없음");
		}
		return ResponseEntity.ok(res);

	}

	@PostMapping("/{studyId}/schedule")
	@ApiOperation(value = "스터디 방 스케줄 입력", notes = "스터디 방 스케줄 입력")
	public ResponseEntity<? extends BaseResponseBody> postStudySchedule(@PathVariable(value = "studyId") Long studyId,
			@RequestBody StudyScheduleReq req) {
		// 값이 다 들어왔는지 확인
		if(req.getTitle().length() == 0 || req.getDate().length() == 0 || req.getColor().length() == 0)
			return ResponseEntity.status(400).body(BaseResponseBody.of(400, "다시 시도해 주세요."));
		// 저장하기
		if (!studyService.postStudySchedule(studyId, req))
			return ResponseEntity.status(400).body(BaseResponseBody.of(400, "다시 시도해 주세요."));

		return ResponseEntity.status(201).body(BaseResponseBody.of(201, "일정 등록 완료"));
	}

	@GetMapping("/{studyId}/score")
	@ApiOperation(value = "문제 푼 점수 결과 가져오기", notes = "통계 페이지를 위한 결과")
	public ResponseEntity<List<StudyTestScoreTotalRes>> getStudyTestScore(
			@PathVariable(value = "studyId") Long studyId) {

		// 스터디에 있는 문제집들 점수 회원별로 가져와서 저장
		List<StudyTestScoreTotalRes> res = studyService.getStudyTestScore(studyId);
		return ResponseEntity.status(200).body(res);

	}

	@GetMapping("/{studyId}/workbook")
	@ApiOperation(value = "문제집 리스트 가져오기", notes = "문제집 리스트 가져오기")
	public ResponseEntity<List<StudyTestListRes>> getStudyTestList(@PathVariable(value = "studyId") Long studyId) {

		List<StudyTestListRes> res = studyService.getStudyTestList(studyId);
		return ResponseEntity.status(200).body(res);
	}

	@GetMapping("/{studyId}/info/member")
	@ApiOperation(value = "스터디 내 멤버들 정보 가져오기", notes = "스터디 내 멤버들 정보 가져오기")
	public ResponseEntity<List<StudyMemberInfoRes>> getStudyMemberInfo(@PathVariable(value = "studyId") Long studyId) {

		List<StudyMemberInfoRes> res = studyService.getStudyMemberInfo(studyId);
		return ResponseEntity.status(200).body(res);
	}

	@PostMapping("/{userId}/score/{testId}")
	@ApiOperation(value = "스터디 문제집 풀이 제출 시 채점 결과 리턴", notes = "스터디 문제집 풀이 제출 시 채점 결과 리턴")
	public ResponseEntity<StudyTestScoreRes> postStudyTestScore(@PathVariable(value = "userId") Long userId,
			@PathVariable(value = "testId") Long testId, @RequestBody List<String> answer) {
		StudyTestScoreRes res = studyService.postStudyTestScore(answer, userId, testId);
		return ResponseEntity.status(200).body(res);

	}

	@GetMapping("/{studyId}/workbook/{testId}")
	@ApiOperation(value = "스터디 문제집 클릭 시 정보 가져오기", notes = "스터디 문제집 클릭 시 정보 가져오기")
	public ResponseEntity<StudyTestInfoRes> getStudyTestInfo(@PathVariable(value = "studyId") Long studyId,
			@PathVariable(value = "testId") Long testId) {

		StudyTestInfoRes res = studyService.getStudyTestInfo(studyId, testId);
		return ResponseEntity.status(200).body(res);
	}

	/* 스터디 문제집 만들기 */
	@PostMapping("/{studyId}")
	@ApiOperation(value = "문제집 만들기", notes = "요청에 따라 문제집을 받고 저장한다.", consumes = "multipart/form-data", produces = "multipart/form-data")
	public ResponseEntity<BaseResponseBody> createWorkbook(
			@ApiParam(value = "로그인 정보", required = true) WorkbookCreatePostReq workbookInfo,
			@RequestPart(value = "study_thumbnail", required = false) MultipartFile testFile,
			@PathVariable(value = "studyId") Long studyId) {

		try {
			Study study = studyService.getStudyById(studyId).get();
			User writer = userService.getUserById(workbookInfo.getTest().getUserId());
			studyTestService.createWorkbook(study, writer, workbookInfo, testFile);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			System.out.println("파일 저장 시 서버 에러");
			return ResponseEntity.status(500).body(new BaseResponseBody(500, "서버 에러"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(" 유저 ,스터디 번호 잘못됨.");
			return ResponseEntity.status(404).body(new BaseResponseBody(404, "존재하지 않는 id 인자값"));
		}

		return ResponseEntity.status(200).body(new BaseResponseBody(200, "문제집 생성 완료"));
	}

	/* 스터디 수정 */
	@PutMapping("/{studyId}/update")
	@ApiOperation(value = "스터디 수정", notes = "스터디 명, 마감인원 등을 받으면 스터디를 생성합니다.", consumes = "multipart/form-data", produces = "multipart/form-data")
	public ResponseEntity<BaseResponseBody> createStudy(
			@ApiParam(value = "로그인 정보", required = true) StudyCreatePostReq studyInfo,
			@RequestPart(value = "study_thumbnail", required = false) MultipartFile studyThumbnail,
			@PathVariable(value = "studyId") Long studyId) {

//		1. 스터디 찾고
		Study study = studyService.getStudyById(studyId).orElse(null);
		if (study == null)
			return ResponseEntity.status(404).body(new BaseResponseBody(404, "존재하지 않는 스터디"));

//		2. 그 스터디 수정하기
		try {
			studyService.updateStudy(study, studyThumbnail, studyInfo);
			return ResponseEntity.status(500).body(new BaseResponseBody(200, "스터디 수정 완료"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(new BaseResponseBody(500, "파싱/파일저장 에러"));
		}

	}

	/* 출석 현황 가져오기 */
	@GetMapping("/{studyId}/attend")
	@ApiOperation(value = "스터디 출석 현황", notes = "스터디멤버 별로 출석 현황을 리턴합니다.")
	public ResponseEntity<?> getAttendList(@PathVariable(value = "studyId") Long studyId) {

//		1. 스터디 찾기
		Study study = studyService.getStudyById(studyId).orElse(null);
		if (study == null)
			return ResponseEntity.status(404).body(new BaseResponseBody(404, "존재하지 않는 스터디"));

		List<AttendGetRes> res = new ArrayList<>();
		
		try {
//		2. 스터디 플랜 찾기
			List<StudyPlan> studyPlans = studyService.getPlansByStudy(studyId);
			
//		3. 멤버 찾기
			if (studyPlans.size() != 0) {

				List<UserStudy> userStudy = userStudyService.getCurrentMember(studyId);
				List<User> members = new ArrayList<User>();
				for (UserStudy us : userStudy) {
					members.add(us.getUser());
				}
				res = studyService.getAttendList(studyPlans, members);
			}

			return ResponseEntity.ok(res);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(new BaseResponseBody(500, "서버 에러"));

		}

	}

	
	@GetMapping("/{studyId}/info")
	@ApiOperation(value = "스터디 방 상세 정보 가져오기", notes = "스터디 방 상세 정보 가져오기")
	public ResponseEntity<List<StudyDetailInfo>> getStudyDetailInfo(@PathVariable(value = "studyId") Long studyId) {
		
		List<StudyDetailInfo> res = studyService.getStudyDetailInfo(studyId);
		
		return ResponseEntity.status(200).body(res);
	}
	
	@PostMapping("/{studyId}/attend/{userId}")
	@ApiOperation(value = "스터디 출석 버튼", notes = "스터디 출석 버튼")
	public ResponseEntity<BaseResponseBody> studyAttendButton(@PathVariable(value = "studyId") Long studyId,
			@PathVariable(value = "userId") Long userId) {
		
		// 해당 멤버의 스터디 출석현황 true로 바꾸기
		String res = studyService.postStudyAttend(studyId, userId);
		if("date".equals(res)) // 스터디 일정 없음
			return ResponseEntity.status(201).body(new BaseResponseBody(201, "오늘 진행 중인 스터디 일정이 없습니다."));
		else if("ok".equals(res)) // 출석 성공
			return ResponseEntity.status(200).body(new BaseResponseBody(200, "출석 완료"));
		else if("already".equals(res))
			return ResponseEntity.status(200).body(new BaseResponseBody(200, "이미 출석했습니다."));
		
		return ResponseEntity.status(400).body(new BaseResponseBody(400, "다시 시도해 주세요."));
	}
	
//	스터디 썸네일
	@GetMapping("/{file}/download")
	@ApiOperation(value = "파일 경로", notes = "<strong>이미지</strong>를 다운로드 한다.")
	public void download(@PathVariable(value = "file") @ApiParam(value = "파일경로", required = true) String file, HttpServletResponse response) throws IOException {
	    //String path = file;
	    String path = "/image/study/";
	    
//	    if(!"default.png".equals(file))
//	    	path += "study/";

	    path += file;
	    String fileNm = file;
	    StringBuffer sb = new StringBuffer(); 
	    for (int i = 0; i < fileNm.length(); i++) 
	    { 
	        char c = fileNm.charAt(i); 
	        if (c > '~') 
	        { 
	            sb.append(URLEncoder.encode(Character.toString(c), "UTF-8")); 
	        } else { 
	            sb.append(c); 
	        } 
	    } 
	    String reFileNm = sb.toString();    
	    response.setContentType("application/octet-stream; charset=UTF-8");// 이번 응답은 html이 아니라 파일이다.
	    response.setHeader("Content-Disposition", "attachment; filename=\""+reFileNm+"\"");
	    response.setHeader("Content-Transfer-Encoding", "binary");
	    try(FileInputStream is = new FileInputStream(path);) {
	        
	        ServletOutputStream os = response.getOutputStream();
	        
	        int data = 0;
	        while((data=is.read())!= -1)
	            os.write(data);
	        
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
//	문제집 썸네일
	@GetMapping("test/{file}/download")
	@ApiOperation(value = "파일 경로", notes = "<strong>이미지</strong>를 다운로드 한다.")
	public void downloadTest(@PathVariable(value = "file") @ApiParam(value = "파일경로", required = true) String file, HttpServletResponse response) throws IOException {
	 
	    //String path = file;
	    String path = "/test/" + file;
	    String fileNm = file;
	    StringBuffer sb = new StringBuffer(); 
	    for (int i = 0; i < fileNm.length(); i++) 
	    { 
	        char c = fileNm.charAt(i); 
	        if (c > '~') 
	        { 
	            sb.append(URLEncoder.encode(Character.toString(c), "UTF-8")); 
	        } else { 
	            sb.append(c); 
	        } 
	    } 
	    String reFileNm = sb.toString();    
	    response.setContentType("application/octet-stream; charset=UTF-8");// 이번 응답은 html이 아니라 파일이다.
	    response.setHeader("Content-Disposition", "attachment; filename=\""+reFileNm+"\"");
	    response.setHeader("Content-Transfer-Encoding", "binary");
	    
	    try(FileInputStream is = new FileInputStream(path)) {
	        
	        ServletOutputStream os = response.getOutputStream();
	        
	        int data = 0;
	        while((data=is.read())!= -1)
	            os.write(data);
	        
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
}
