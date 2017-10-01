package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import dao.{StatusDAO, UserDAO}
import models.StatusWithUser
import utilities.ApplicationError

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@ImplementedBy(classOf[StatusServiceImpl])
trait StatusService {
  def getAllStatuses(userID:Int)(implicit executor:ExecutionContext):Future[Either[ApplicationError, Seq[StatusWithUser]]]
}


class StatusServiceImpl @Inject() (statusDAO: StatusDAO,userService: UserService,userDAO: UserDAO) extends StatusService{
  override def getAllStatuses(userID:Int)(implicit executor:ExecutionContext): Future[Either[ApplicationError,Seq[StatusWithUser]]] = {
    userService.existUser(userID).map {
      case Left(error) => Left(error)
      case Right(exist) => {
        if(exist == false){
           Left(ApplicationError("ユーザが見つかりませんでした"))
        } else{
          val future = statusDAO.getAll().map{statuses=>
            Await.result(userDAO.findIn(statuses.map{s=>s.userID}.distinct).map{users=>
              statuses.map{status=>
                StatusWithUser(status.id,status.userID,users.filter(u=>u.id == status.userID).head.name,status.text)
              }
            },Duration.Inf)
          }
          try{
            Right(Await.result(future,Duration.Inf))
          } catch {
            case e:Exception => Left(ApplicationError("データ処理に失敗しました",e))
          }
        }
      }
    }
  }
}